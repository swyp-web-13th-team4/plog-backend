package com.plog.plogbackend.global.config;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        //TODO : OAuth 설정 , JWT 설정 , 기본 formLogin 비활성화 (Jwt 로그인 필터로 대체) , 에러 헨들러 구현 , 패스워드 인코더 결정

        http
                // 개발 전용 csrf 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 완전 비활성(STATELESS)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 기본 formLogin 비활성 (JwtLoginFilter로 대체)
                .formLogin(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                                .requestMatchers( "/swagger-ui/**").permitAll() // 테스트 전용
                                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll() // TODO : Oauth API 엔드포인트 확인후 재설정

                                //.anyRequest().authenticated() // 운영 기준
                                .anyRequest().permitAll()    // 테스트 기준
                );
        return http.build();
    }



}
