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
                      config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
                      config.setAllowedHeaders(List.of("*"));
                      config.setAllowCredentials(true);
                      return config;
                    }))

        // 2. 세션 정책: OAuth2 로그인 흐름(state 검증)을 위해 IF_REQUIRED 사용
        // STATELESS 설정 시 OAuth2 콜백 시점에 state를 비교할 세션이 없어 인증 실패함
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

        // 3. 권한 및 경로 설정
        .authorizeHttpRequests(
            auth ->
                auth
                    // Swagger 등 API 문서
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()

                    // 회원가입 API와 소셜 로그인
                    .requestMatchers(
                        "/api/members/signup",
                        "/api/members/refresh",
                        "/api/members/logout",
                        "/oauth2/**",
                        "/login/**")
                    .permitAll()

                    // 테스트용 게시글 이미지 목록 조회 TODO: 게시글 API 구현 완료후 삭제
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/test/images/**")
                    .permitAll()

                    // 내 프로필 이미지 관리, 테스트용 게시글 이미지 등록/삭제 TODO: 마이페이지 , 게시글 API 구현 완료후 삭제
                    .requestMatchers("/api/members/me/profile-image", "/api/test/images/upload")
                    .authenticated()

                    // 그 외 모든 요청은 인증(JWT) 필요 (현재는 테스트를 위해 열어둠)
                    // .anyRequest().authenticated()

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
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler))

        // 5. JWT 필터를 시큐리티 기본 필터 앞에 추가
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
