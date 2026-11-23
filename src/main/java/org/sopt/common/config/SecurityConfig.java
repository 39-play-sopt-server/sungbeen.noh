package org.sopt.common.config;

import jakarta.servlet.DispatcherType;
import org.sopt.auth.security.AuthOriginFilter;
import org.sopt.auth.security.JwtAuthenticationFilter;
import org.sopt.auth.security.JwtTokenVerifier;
import org.sopt.auth.security.SecurityErrorHandler;
import org.sopt.member.repository.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenVerifier jwtTokenVerifier,
                                          MemberRepository memberRepository,
                                          BrowserOrigins browserOrigins,
                                          SecurityErrorHandler errorHandler) throws Exception {
        var paths = PathPatternRequestMatcher.withDefaults();
        var cookieRequests = new OrRequestMatcher(
                paths.matcher(HttpMethod.POST, "/api/v1/auth/login"),
                paths.matcher(HttpMethod.POST, "/api/v1/auth/refresh"),
                paths.matcher(HttpMethod.POST, "/api/v1/auth/logout"));

        return http
                // 인증 쿠키를 설정·조회·삭제하는 요청은 Origin 검사로 CSRF 대응
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(cookieRequests).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/signup",
                                "/api/v1/members").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                // CORS 사전 요청을 먼저 처리한 뒤 쿠키를 쓰는 POST 요청의 Origin 확인
                .addFilterAfter(new AuthOriginFilter(cookieRequests, browserOrigins, errorHandler), CorsFilter.class)
                // 경로별 접근 권한을 확인하기 전에 JWT로 인증 정보를 채움
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenVerifier, memberRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
