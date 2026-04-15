package com.plog.plogbackend.security;

import java.util.Collections;
import java.util.Map;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    // 기본 설정을 통해 유저 정보를 가져옴
    OAuth2User oAuth2User = super.loadUser(userRequest);

    // 카카오 로그인은 "id" 필드가 고유 식별자
    String userNameAttributeName =
        userRequest
            .getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName();

    Map<String, Object> attributes = oAuth2User.getAttributes();

    // 인증 정보를 전달하는 역할만 수행하며, 실제 처리는 SuccessHandler에서 진행합니다.
    return new DefaultOAuth2User(Collections.emptyList(), attributes, userNameAttributeName);
  }
}
