package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.dto.MemberSignupRequest;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.security.jwt.JwtProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final JwtProvider jwtProvider;

  @Transactional
  public UUID signup(String registerToken, MemberSignupRequest request) {
    if (!jwtProvider.isValidToken(registerToken) || !jwtProvider.isRegisterToken(registerToken)) {
      throw new AppException(ErrorType.INVALID_SIGNUP_TOKEN);
    }

    String providerId = jwtProvider.getProviderIdFromToken(registerToken);
    if (memberRepository.findByProviderId(providerId).isPresent()) {
      throw new AppException(ErrorType.ALREADY_REGISTERED_MEMBER);
    }

    Member member = Member.createNewMember(request.nickname(), providerId, request.profileImage());
    memberRepository.save(member);

    return member.getMemberKey(); // accessToken 생성은 컨트롤러에서 처리
  }
}
