package com.plog.plogbackend.global.config;

import com.plog.plogbackend.security.CustomOAuth2UserService;
import com.plog.plogbackend.security.error.OAuth2FailureHandler;
import com.plog.plogbackend.security.jwt.JwtAuthenticationFilter;
import com.plog.plogbackend.security.oauth2.OAuth2SuccessHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
  // yml 파일에서 주소를 읽어옵니다.
  @Value("${spring.security.front.cors.allowed-origins}")
  private String allowedOrigins;

  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final OAuth2FailureHandler oAuth2FailureHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // 자체 로그인 방식 비활성화
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)

        // CORS 설정 (setAllowedOrigins 통해서 허용 URL 결정)
        .cors(
            cors ->
                cors.configurationSource(
                    request -> {
                      CorsConfiguration config = new CorsConfiguration();
                      config.setAllowedOrigins(List.of(allowedOrigins)); // 허용 주소
                      config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
                      config.setAllowedHeaders(List.of("*"));
                      config.setAllowCredentials(true);
                      return config;
                    }))

        // 2. 세션 사용 안 함 (JWT 사용)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // 3. 권한 및 경로 설정
        .authorizeHttpRequests(
            auth ->
                auth
                    // 회원가입 API와 소셜 로그인 진입점은 모두에게 허용
                    .requestMatchers("/api/members/signup", "/oauth2/**", "/login/**")
                    .permitAll()
                    // 그 외 모든 요청은 인증(JWT) 필요
                    //                    .anyRequest().authenticated()

                    // 테스트 전용
                    .anyRequest()
                    .permitAll())

        // 4. 소셜 로그인(OAuth2) 설정
        .oauth2Login(
            oauth2 ->
                oauth2
                    .userInfoEndpoint(
                        userInfo -> userInfo.userService(customOAuth2UserService) // 카카오 유저 정보 수집
                        )
                    .successHandler(oAuth2SuccessHandler) // 가져온 후 성공 처리
                    .failureHandler(oAuth2FailureHandler) // <--- 이거 추가!
            )

        // 5. JWT 필터를 시큐리티 기본 필터 앞에 추가
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
