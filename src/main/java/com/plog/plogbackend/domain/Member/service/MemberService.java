package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.dto.MemberSignupRequest;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.security.jwt.JwtProvider;
import com.plog.plogbackend.security.jwt.RefreshTokenService;
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
  private final RefreshTokenService refreshTokenService;

  /** 회원가입: 회원 저장 후 Access/Refresh Token을 발급하여 반환합니다. */
  @Transactional
  public RefreshTokenService.TokenPair signup(
      String registerToken, MemberSignupRequest request, MultipartFile profileImage) {

    if (!jwtProvider.isValidToken(registerToken) || !jwtProvider.isRegisterToken(registerToken)) {
      throw new AppException(ErrorType.INVALID_SIGNUP_TOKEN);
    }

    String providerId = jwtProvider.getProviderIdFromToken(registerToken);
    if (memberRepository.findByProviderId(providerId).isPresent()) {
      throw new AppException(ErrorType.ALREADY_REGISTERED_MEMBER);
    }

    String profileImageUrl = memberImageService.uploadSignupProfileImage(profileImage);
    Member member = Member.createNewMember(request.nickname(), providerId, profileImageUrl);
    memberRepository.save(member);

    String accessToken = jwtProvider.createAccessToken(member.getMemberKey());
    String refreshToken = refreshTokenService.createRefreshToken(member.getMemberKey());
    return new RefreshTokenService.TokenPair(accessToken, refreshToken);
  }

  /** 로그아웃: DB에서 Refresh Token을 삭제합니다. */
  @Transactional
  public void logout(UUID memberKey) {
    refreshTokenService.deleteRefreshToken(memberKey);
  }

  /** 토큰 갱신: Refresh Token을 검증하고 새 Access/Refresh Token 쌍을 반환합니다. */
  @Transactional
  public RefreshTokenService.TokenPair refreshToken(String refreshToken) {
    if (refreshToken == null) {
      throw new AppException(ErrorType.INVALID_REFRESH_TOKEN);
    }
    return refreshTokenService.refreshAccessToken(refreshToken);
  }
}
