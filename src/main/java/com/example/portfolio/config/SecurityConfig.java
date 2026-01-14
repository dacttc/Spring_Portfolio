package com.example.portfolio.config;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Slf4j
@Configuration
public class SecurityConfig {
    public static final String RESEND_USERNAME_SESSION_KEY = "RESEND_USERNAME";
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // REST API는 CSRF 제외
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/viewer/**",
                                "/models/**",
                                "/**/*.gltf", "/**/*.glb", "/**/*.bin",
                                "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.webp","/resend-verification","/verify","/signup","/test-email","/css/**", "/js/**").permitAll()
                        // 도시 맵 API - GET은 공개, 나머지는 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/map/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/map/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/map/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/map/**").authenticated()
                        // 도시 페이지 - 누구나 볼 수 있음 (/{username} 또는 /{username}/{cityName} 형식)
                        .requestMatchers(request -> {
                            String path = request.getServletPath();
                            // /{username} 경로 허용 (username은 영문숫자_만)
                            // /{username}/{cityName} 경로 허용 (cityName은 URL 인코딩된 한글 포함)
                            // login, admin 등 예약어는 CityController에서 처리
                            return path.matches("^/[a-zA-Z0-9_]+$") ||
                                   path.matches("^/[a-zA-Z0-9_]+/.+$");
                        }).permitAll()
                        .requestMatchers("/mypage").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler((request, response, exception) -> {

                            // 로그인 폼 input name="username" 값
                            String inputUsername = request.getParameter("username");

                            // 동시 로그인 차단 (이미 다른 곳에서 로그인 중)
                            Throwable cause = exception.getCause();
                            if (exception instanceof SessionAuthenticationException ||
                                    cause instanceof SessionAuthenticationException) {
                                log.warn("동시 로그인 차단: {}", inputUsername);
                                response.sendRedirect("/login?maxSession");
                                return;
                            }

                            // DisabledException이 직접 오거나, cause에 숨어있을 수 있음
                            boolean notVerified =
                                    exception instanceof DisabledException ||
                                            cause instanceof DisabledException;

                            if (notVerified) {
                                // ✅ 여기서 세션에 username 저장
                                HttpSession session = request.getSession(true);
                                session.setAttribute(RESEND_USERNAME_SESSION_KEY, inputUsername);

                                response.sendRedirect("/resend-verification?error=not_verified");
                                return;
                            }

                            response.sendRedirect("/login?error");
                        })
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .logoutSuccessUrl("/?logout")
                        .deleteCookies("JSESSIONID", "remember-me")
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecretKey-city-builder-2024")  // 토큰 암호화 키
                        .tokenValiditySeconds(60 * 60 * 24 * 30)      // 30일 유지
                        .rememberMeParameter("remember-me")           // 체크박스 name
                        .rememberMeCookieName("remember-me")          // 쿠키 이름
                )
                // 동시 접속 제한 - 한 계정당 1개 세션만 허용
                .sessionManagement(session -> session
                        .maximumSessions(1)                           // 최대 세션 수: 1
                        .maxSessionsPreventsLogin(true)               // true: 이미 로그인 중이면 새 로그인 차단
                        .expiredUrl("/login?expired")                 // 세션 만료 시 리다이렉트
                        .sessionRegistry(sessionRegistry())
                );

        return http.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // 세션 이벤트 발행 (세션 생성/파괴 감지에 필요)
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
