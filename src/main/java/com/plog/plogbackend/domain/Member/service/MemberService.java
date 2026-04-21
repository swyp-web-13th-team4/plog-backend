package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.dto.MemberSignupRequest;
import com.plog.plogbackend.domain.Member.dto.MyPageMemberDTO;
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

  @Transactional
  public UUID signup(
      String registerToken, MemberSignupRequest request, MultipartFile profileImage) {
    if (!jwtProvider.isValidToken(registerToken) || !jwtProvider.isRegisterToken(registerToken)) {
      throw new AppException(ErrorType.INVALID_SIGNUP_TOKEN);
    }

    String providerId = jwtProvider.getProviderIdFromToken(registerToken);
    if (memberRepository.findByProviderId(providerId).isPresent()) {
      throw new AppException(ErrorType.ALREADY_REGISTERED_MEMBER);
    }

    // 이미지 업로드는 MemberImageService에서 처리
    String profileImageUrl = memberImageService.uploadSignupProfileImage(profileImage);

    Member member = Member.createNewMember(request.nickname(), providerId, profileImageUrl);
    memberRepository.save(member);


    return member.getMemberKey(); // accessToken 생성은 컨트롤러에서 처리
  }

  // 회원 조회 (닉네임, 프로필 사진, 대표 뱃지)
  @Transactional(readOnly = true)
  public MyPageMemberDTO getMyPageInfo(UUID memberKey) {
    Member member = memberRepository.findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    return MyPageMemberDTO.builder()
        .nickname(member.getNickname())
        .profileImageUrl(member.getProfileImage())
            // TODO : 뱃지
        .build();
  }
}
