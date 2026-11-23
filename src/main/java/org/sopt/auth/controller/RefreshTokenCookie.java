package org.sopt.auth.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

// Refresh Token 쿠키 생성과 만료 처리
@Component
public class RefreshTokenCookie {

    static final String NAME = "refresh_token";
    private static final String PATH = "/api/v1/auth";

    private final Duration lifetime;
    private final boolean secure;

    public RefreshTokenCookie(@Value("${jwt.refresh-token-expiration}") long lifetimeMillis,
                              @Value("${app.auth.refresh-cookie-secure}") boolean secure) {
        this.lifetime = Duration.ofMillis(lifetimeMillis);
        this.secure = secure;
    }

    public ResponseCookie create(String refreshToken) {
        return build(refreshToken, lifetime);
    }

    public ResponseCookie expire() {
        // 발급 때와 같은 이름과 경로로 Max-Age=0을 내려 기존 쿠키 삭제
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration maxAge) {
        // 브라우저가 인증 경로에서만 보내도록 경로 제한 / 자바스크립트에서 읽을 수 없도록 설정
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(PATH)
                .maxAge(maxAge)
                .build();
    }
}
