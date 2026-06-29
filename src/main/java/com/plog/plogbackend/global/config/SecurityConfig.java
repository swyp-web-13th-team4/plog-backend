package com.plog.plogbackend.global.config;

import com.plog.plogbackend.security.error.CustomAccessDeniedHandler;
import com.plog.plogbackend.security.error.CustomAuthenticationEntryPoint;
import com.plog.plogbackend.security.error.OAuth2FailureHandler;
import com.plog.plogbackend.security.jwt.JwtAuthenticationFilter;
import com.plog.plogbackend.security.oauth2.CustomOAuth2UserService;
import com.plog.plogbackend.security.oauth2.OAuth2SuccessHandler;
import jakarta.servlet.DispatcherType;
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
import org.springframework.security.web.context.NullSecurityContextRepository;
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
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;

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
                      config.setAllowedOrigins(
                          java.util.Arrays.stream(allowedOrigins.split(","))
                              .map(String::trim)
                              .toList()); // 허용 주소
                      config.setAllowedMethods(
                          List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                      config.setAllowedHeaders(List.of("*"));
                      config.setAllowCredentials(true);
                      return config;
                    }))

        // 2. 세션 정책: OAuth2 로그인 흐름(state 검증)을 위해 IF_REQUIRED 사용
        // STATELESS 설정 시 OAuth2 콜백 시점에 state를 비교할 세션이 없어 인증 실패함
        // 단, 인증 컨텍스트는 세션에 저장하지 않음 → JSESSIONID로 JWT 우회 방지
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .securityContext(ctx -> ctx.securityContextRepository(new NullSecurityContextRepository()))

        // 3. 권한 및 경로 설정
        .authorizeHttpRequests(
            auth ->
                auth
                    // SSE async dispatch 재진입 시 인증 컨텍스트가 없으므로 ASYNC는 무조건 통과
                    // (Tomcat이 SSE 완료 후 내부적으로 ASYNC 타입 재디스패치를 하기 때문)
                    .dispatcherTypeMatchers(DispatcherType.ASYNC)
                    .permitAll()

                    // Swagger 등 API 문서
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()

                    // 어드민 페이지 정적 리소스 (CSS, JS)
                    .requestMatchers("/css/admin/**", "/js/admin/**")
                    .permitAll()

                    // 회원가입 API와 소셜 로그인, 테스트 로그인
                    .requestMatchers(
                        "/api/members/signup",
                        "/api/members/refresh",
                        "/api/members/logout",
                        "/api/auth/callback",
                        "/oauth2/**",
                        "/login/**",
                        "/testlogin",
                        "/testlogin/**")
                    .permitAll()

                    // 약관
                    .requestMatchers("/api/terms")
                    .permitAll()
                    .requestMatchers(
                        "/actuator/**", "/api/members/default-images", "/api/members/validate/**")
                    .permitAll()

                    // 피드 관련 조회 API 허용
                    .requestMatchers(
                        org.springframework.http.HttpMethod.GET,
                        "/api/feed/list",
                        "/api/feed/{postId}",
                        "/api/feed/profileView/**")
                    .permitAll()

                    // 테스트용 게시글 이미지 목록 조회 TODO: 게시글 API 구현 완료후 삭제
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/test/images/**")
                    .permitAll()

                    // 인증이 필요한 모든 도메인 API 명시적 추가
                    .requestMatchers(
                        // 피드 관련 (수정/좋아요/북마크 등)
                        "/api/feed/bookmark/**",
                        "/api/feed/like/**",

                        // 환경 기록(게시글) 관련
                        "/api/post/**",

                        // 최근 검색어 관련
                        "/api/place/recent/**",

                        // 지도 관련
                        "/api/map/**",

                        // 내 프로필 및 회원 정보 관련
                        "/api/members/me",
                        "/api/members/me/**",
                        "/api/members/validate/**",
                        "/api/members/default-images",

                        // 마이페이지 관련
                        "/api/members/mypage",
                        "/api/members/mypage/**",
                        "/api/members/bookmark",
                        "/api/members/badge",
                        "/api/members/badge/**",
                        "/api/members/analytics",
                        "/api/notification/**")
                    .authenticated()

                    // 내 프로필 이미지 관리, 테스트용 게시글 이미지 등록/삭제 TODO: 마이페이지 , 게시글 API 구현 완료후 삭제
                    .requestMatchers("/api/test/images/upload")
                    .permitAll()

                    // 어드민 페이지 접근 권한 (단, 에러 페이지는 모두 접근 가능)
                    .requestMatchers("/admin/error")
                    .permitAll()
                    .requestMatchers("/admin", "/admin/**")
                    .hasRole("ADMIN")

                    // 그 외 모든 요청은 인증(JWT) 필요
                    .anyRequest()
                    .authenticated())
        //                    .permitAll())

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
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        // 6. 예외 처리 설정 (401, 403)
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler));

    return http.build();
  }
}
