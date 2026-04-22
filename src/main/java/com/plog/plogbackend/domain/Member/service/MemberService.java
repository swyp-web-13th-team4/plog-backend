package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.dto.MemberSignupRequest;
import com.plog.plogbackend.domain.Member.dto.MyPageMemberResponse;
import com.plog.plogbackend.domain.Member.dto.UpdateProfileRequest;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.security.jwt.JwtProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final JwtProvider jwtProvider;
  private final MemberImageService memberImageService;

  /**
   * 회원가입을 처리합니다.
   *
   * <p>프로필 이미지는 파일 업로드 또는 기본 이미지 URL 중 하나를 반드시 제공해야 합니다.
   * 이미지 결정은 {@link MemberImageService#resolveSignupProfileImage}에 위임합니다.
   *
   * @param registerToken 카카오 인증 후 발급된 임시 가입 토큰
   * @param request       닉네임, 약관동의 정보
   * @param profileImage  직접 업로드할 이미지 파일 (nullable)
   * @param defaultImageId 기본 이미지 ID (profileImage가 없을 때 사용, nullable)
   * @return 생성된 회원의 memberKey
   */
  @Transactional
  public UUID signup(
      String registerToken, MemberSignupRequest request,
      MultipartFile profileImage, Long defaultImageId) {

    if (!jwtProvider.isValidToken(registerToken) || !jwtProvider.isRegisterToken(registerToken)) {
      throw new AppException(ErrorType.INVALID_SIGNUP_TOKEN);
    }

    String providerId = jwtProvider.getProviderIdFromToken(registerToken);
    if (memberRepository.findByProviderId(providerId).isPresent()) {
      throw new AppException(ErrorType.ALREADY_REGISTERED_MEMBER);
    }

    // 파일 또는 기본 이미지 ID 중 하나는 반드시 있어야 함 (없으면 FILE_EMPTY 예외)
    String profileImageUrl = memberImageService.resolveSignupProfileImage(profileImage, defaultImageId);

    Member member = Member.createNewMember(request.nickname(), providerId, profileImageUrl);
    memberRepository.save(member);

    return member.getMemberKey();
  }

  /**
   * 마이페이지 정보를 조회합니다.
   *
   * @param memberKey 회원 UUID
   * @return 닉네임, 프로필 이미지 URL
   */
  @Transactional(readOnly = true)
  public MyPageMemberResponse getMyPageInfo(UUID memberKey) {
    Member member = memberRepository.findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    return MyPageMemberResponse.builder()
        .nickname(member.getNickname())
        .profileImageUrl(member.getProfileImage())
        .introduction(member.getIntroduction())
        // TODO : 뱃지
        .build();
  }

  /**
   * 프로필(닉네임 + 이미지)을 한 번에 수정합니다.
   *
   * <p>이미지가 제공된 경우:
   * <ul>
   *   <li>파일이면 GCS에 업로드 후 URL 저장, 기존 커스텀 이미지는 커밋 후 GCS 삭제</li>
   *   <li>기본 이미지 ID이면 DB 검증 후 해당 URL로 교체</li>
   * </ul>
   * <p>닉네임이 null 또는 빈 값이면 닉네임은 변경하지 않습니다.
   * <p>이미지가 null이고 defaultImageId도 null이면 이미지는 변경하지 않습니다.
   *
   * @param memberKey 회원 UUID
   * @param request   닉네임 변경 정보 (nullable 허용)
   * @param file      업로드할 이미지 파일 (nullable)
   * @param defaultImageId  기본 이미지 ID (file이 없을 때 사용, nullable)
   */
  @Transactional
  public void updateProfile(
      UUID memberKey, UpdateProfileRequest request, MultipartFile file, Long defaultImageId) {

    Member member = memberRepository.findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    // 이미지 변경 처리 (변경 없으면 null 반환)
    String newImageUrl = memberImageService.resolveAndScheduleImageUpdate(member, file, defaultImageId);

    // 닉네임 + 이미지 + 소개글 한 번에 적용
    String newNickname = (request != null) ? request.nickname() : null;
    String newIntroduction = (request != null) ? request.introduction() : null;
    member.updateProfile(newNickname, newImageUrl, newIntroduction);

    log.info("프로필 수정 완료 - memberKey: {}, nickname: {}, imageUrl: {}, introduction: {}",
        memberKey, member.getNickname(), member.getProfileImage(), member.getIntroduction());
  }
}
