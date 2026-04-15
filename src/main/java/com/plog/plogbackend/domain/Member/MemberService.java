package com.plog.plogbackend.domain.Member;

import com.plog.plogbackend.domain.Member.dto.MemberSignupRequest;
import com.plog.plogbackend.domain.Member.dto.MemberSignupResponse;
import com.plog.plogbackend.security.jwt.JwtProvider;
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
  public MemberSignupResponse signup(String registerToken, MemberSignupRequest request) {
    if (!jwtProvider.isValidToken(registerToken) || !jwtProvider.isRegisterToken(registerToken)) {
      throw new IllegalArgumentException("유효하지 않은 가입 토큰입니다.");
    }

    String providerId = jwtProvider.getProviderIdFromToken(registerToken);
    if (memberRepository.findByProviderId(providerId).isPresent()) {
      throw new IllegalStateException("이미 가입된 회원입니다.");
    }

    Member member = Member.createNewMember(request.nickname(), providerId, request.profileImage());
    memberRepository.save(member);

    String accessToken = jwtProvider.createAccessToken(member.getMemberKey());
    return new MemberSignupResponse(accessToken);
  }
}
