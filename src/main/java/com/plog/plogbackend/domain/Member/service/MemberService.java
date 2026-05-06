package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.MemberAgreement;
import com.plog.plogbackend.domain.Member.dto.request.MemberSignupRequest;
import com.plog.plogbackend.domain.Member.dto.request.UpdateProfileRequest;
import com.plog.plogbackend.domain.Member.dto.response.MemberBadgeResponse;
import com.plog.plogbackend.domain.Member.dto.response.MemberResponse;
import com.plog.plogbackend.domain.Member.entity.Terms;
import com.plog.plogbackend.domain.Member.repository.MemberAgreementRepository;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.Member.repository.TermsRepository;
import com.plog.plogbackend.domain.badge.event.BadgeGrantEvent;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.security.jwt.JwtProvider;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

  /** 첫 로그인(회원가입) 뱃지 ID */
  private static final long BADGE_ID_FIRST_LOGIN = 1L;

  /** 유효성 검사 패턴. (메모리 낭비 방지) */
  private static final Pattern PHONE_PATTERN =
      Pattern.compile("(?:010|02|0[3-9]{2})[-.\\s]?\\d{3,4}[-.\\s]?\\d{4}");

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
  private static final Pattern SNS_PATTERN =
      Pattern.compile(
          "kakao|카카오|카톡|insta|인스타|facebook|페이스북|페북|twitter|트위터|telegram|텔레그램|line|라인|@[a-zA-Z0-9._]+");

  private final MemberRepository memberRepository;
  private final JwtProvider jwtProvider;
  private final MemberImageService memberImageService;
  private final TermsRepository termsRepository;
  private final MemberAgreementRepository memberAgreementRepository;
  private final BadWordFilterService badWordFilterService;

  // 이벤트 처리 객체
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 회원가입을 처리합니다.
   *
   * <p>프로필 이미지는 파일 업로드 또는 기본 이미지 URL 중 하나를 반드시 제공해야 합니다. 이미지 결정은 {@link
   * MemberImageService#resolveSignupProfileImage}에 위임합니다.
   *
   * @param registerToken 카카오 인증 후 발급된 임시 가입 토큰
   * @param request 닉네임, 약관동의 정보
   * @param profileImage 직접 업로드할 이미지 파일 (nullable)
   * @param defaultImageId 기본 이미지 ID (profileImage가 없을 때 사용, nullable)
   * @return 생성된 회원의 memberKey
   */
  @Transactional
  public UUID signup(
      String registerToken,
      MemberSignupRequest request,
      MultipartFile profileImage,
      Long defaultImageId) {

    if (!jwtProvider.isValidToken(registerToken) || !jwtProvider.isRegisterToken(registerToken)) {
      throw new AppException(ErrorType.INVALID_SIGNUP_TOKEN);
    }

    String providerId = jwtProvider.getProviderIdFromToken(registerToken);
    if (memberRepository.findByProviderId(providerId).isPresent()) {
      throw new AppException(ErrorType.ALREADY_REGISTERED_MEMBER);
    }

    // 닉네임, 소개글 유효성 검사
    validateNickname(request.nickname(), null);
    validateIntroduction(request.introduction());

    // 파일 또는 기본 이미지 ID 중 하나는 반드시 있어야 함 (없으면 FILE_EMPTY 예외)
    String profileImageUrl =
        memberImageService.resolveSignupProfileImage(profileImage, defaultImageId);

    Member member =
        Member.createNewMember(
            request.nickname(), providerId, profileImageUrl, request.introduction());
    memberRepository.save(member);

    if (request.termsAgreements() != null) {
      for (Map.Entry<String, Boolean> entry : request.termsAgreements().entrySet()) {
        Terms terms =
            termsRepository
                .findByName(entry.getKey())
                .orElseThrow(() -> new AppException(ErrorType.TERMS_NOT_FOUND));

        if (terms.isRequired() && !entry.getValue()) {
          throw new AppException(ErrorType.REQUIRED_TERMS_NOT_AGREED);
        }

        MemberAgreement agreement =
            MemberAgreement.builder()
                .member(member)
                .terms(terms)
                .isAgreed(entry.getValue())
                .build();
        memberAgreementRepository.save(agreement);
      }
    }

    // 첫 로그인 뱃지(id:1) 부여 이벤트 발행
    // - 트랜잭션 커밋 후 BadgeEventHandler가 독립 트랜잭션으로 처리
    eventPublisher.publishEvent(new BadgeGrantEvent(member.getId(), BADGE_ID_FIRST_LOGIN));

    return member.getMemberKey();
  }

  /**
   * 마이페이지 정보를 조회합니다.
   *
   * @param memberKey 회원 UUID
   * @return 닉네임, 프로필 이미지 URL , 소개글
   */
  @Transactional(readOnly = true)
  public MemberResponse getMyPageInfo(UUID memberKey) {
    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    MemberBadgeResponse badgeResponse = null;
    if (member.getMainBadge() != null) {
      badgeResponse = MemberBadgeResponse.of(member.getMainBadge(), true);
    }

    return MemberResponse.builder()
        .nickname(member.getNickname())
        .profileImageUrl(member.getProfileImage())
        .introduction(member.getIntroduction())
        .mainBadge(badgeResponse)
        .build();
  }

  /**
   * 프로필(닉네임 + 이미지)을 한 번에 수정합니다.
   *
   * <p>이미지가 제공된 경우:
   *
   * <ul>
   *   <li>파일이면 GCS에 업로드 후 URL 저장, 기존 커스텀 이미지는 커밋 후 GCS 삭제
   *   <li>기본 이미지 ID이면 DB 검증 후 해당 URL로 교체
   * </ul>
   *
   * <p>닉네임이 null 또는 빈 값이면 닉네임은 변경하지 않습니다.
   *
   * <p>이미지가 null이고 defaultImageId도 null이면 이미지는 변경하지 않습니다.
   *
   * @param memberKey 회원 UUID
   * @param request 닉네임 변경 정보 (nullable 허용)
   * @param file 업로드할 이미지 파일 (nullable)
   * @param defaultImageId 기본 이미지 ID (file이 없을 때 사용, nullable)
   */
  @Transactional
  public void updateProfile(
      UUID memberKey, UpdateProfileRequest request, MultipartFile file, Long defaultImageId) {

    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    // 이미지 변경 처리 (변경 없으면 null 반환)
    String newImageUrl =
        memberImageService.resolveAndScheduleImageUpdate(member, file, defaultImageId);

    String newNickname = request.nickname();
    String newIntroduction = request.introduction();

    // 닉네임 유효성 검사 및 중복 확인
    validateNickname(newNickname, memberKey);

    // 소개글 유효성 검사 (개인정보 및 SNS 계정 포함 방지)
    validateIntroduction(newIntroduction);

    // 닉네임 + 이미지 + 소개글 한 번에 적용
    member.updateProfile(newNickname, newImageUrl, newIntroduction);

    log.info(
        "프로필 수정 완료 - memberKey: {}, nickname: {}, imageUrl: {}, introduction: {}",
        memberKey,
        member.getNickname(),
        member.getProfileImage(),
        member.getIntroduction());
  }

  /** 닉네임 유효성 검사 및 중복 확인 */
  public void validateNickname(String nickname, UUID memberKey) {
    if (nickname == null || nickname.isBlank()) {
      return;
    }

    if (!nickname.matches("^[가-힣a-zA-Z0-9_]+$")) {
      throw new AppException(ErrorType.INVALID_NICKNAME_FORMAT);
    }

    // 금칙어 검사
    badWordFilterService.validate(nickname);

    boolean isOwnNickname = false;
    if (memberKey != null) {
      Member member = memberRepository.findByMemberKey(memberKey).orElse(null);
      if (member != null && nickname.equals(member.getNickname())) {
        isOwnNickname = true;
      }
    }

    if (!isOwnNickname && memberRepository.existsByNickname(nickname)) {
      throw new AppException(ErrorType.DUPLICATE_NICKNAME);
    }
  }

  /** 소개글 유효성 검사 (개인정보 및 SNS 방지) */
  public void validateIntroduction(String introduction) {
    if (introduction == null || introduction.isBlank()) {
      return;
    }

    String lowerIntro = introduction.toLowerCase();

    // 2. matches(".*패턴.*") 대신 부분 검색에 최적화된 matcher().find() 사용
    boolean hasPhoneNumber = PHONE_PATTERN.matcher(lowerIntro).find();
    boolean hasEmail = EMAIL_PATTERN.matcher(lowerIntro).find();
    boolean hasSnsKeyword = SNS_PATTERN.matcher(lowerIntro).find();

    if (hasPhoneNumber || hasEmail || hasSnsKeyword) {
      throw new AppException(ErrorType.INVALID_INTRODUCTION_FORMAT);
    }

    // 금칙어 검사
    badWordFilterService.validate(introduction);
  }
}
