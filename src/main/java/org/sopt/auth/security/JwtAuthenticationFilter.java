package org.sopt.auth.security;

import java.io.IOException;
import java.util.List;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sopt.member.repository.MemberRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// SecurityFilterChain에서만 실행하도록 별도 Component로 등록하지 않음
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenVerifier jwtTokenVerifier;
    private final MemberRepository memberRepository;

    public JwtAuthenticationFilter(JwtTokenVerifier jwtTokenVerifier, MemberRepository memberRepository) {
        this.jwtTokenVerifier = jwtTokenVerifier;
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            Long memberId = null;
            try {
                memberId = jwtTokenVerifier.verifyAccessToken(header.substring(BEARER_PREFIX.length()));
            } catch (JwtException | IllegalArgumentException exception) {
                // 인증 정보 없이 다음 필터로 넘김 / 보호된 경로는 이후 접근 검사에서 401 응답
                SecurityContextHolder.clearContext();
            }
            // 탈퇴한 회원의 토큰은 만료 전이라도 사용하지 못하도록 현재 회원 확인
            if (memberId != null && memberRepository.existsById(memberId)) {
                // 토큰 검증과 회원 조회를 마친 뒤 인증 완료 상태로 등록
                // 회원 ID는 principal로 저장해 컨트롤러의 @AuthenticationPrincipal로 전달
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(memberId, null, List.of()));
                SecurityContextHolder.setContext(context);
            }
        }
        filterChain.doFilter(request, response);
    }
}
