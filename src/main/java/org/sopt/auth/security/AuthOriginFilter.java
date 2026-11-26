package org.sopt.auth.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sopt.common.config.BrowserOrigins;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

// 브라우저가 쿠키를 자동으로 보내므로 인증 요청의 출처를 별도로 확인
public class AuthOriginFilter extends OncePerRequestFilter {

    private final RequestMatcher protectedRequests;
    private final BrowserOrigins browserOrigins;
    private final SecurityErrorHandler errorHandler;

    public AuthOriginFilter(RequestMatcher protectedRequests, BrowserOrigins browserOrigins,
                            SecurityErrorHandler errorHandler) {
        this.protectedRequests = protectedRequests;
        this.browserOrigins = browserOrigins;
        this.errorHandler = errorHandler;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !protectedRequests.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!browserOrigins.allows(request.getHeader(HttpHeaders.ORIGIN))) {
            errorHandler.handle(request, response, new AccessDeniedException("허용되지 않은 출처입니다."));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
