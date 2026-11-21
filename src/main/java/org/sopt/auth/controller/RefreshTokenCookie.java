package org.sopt.auth.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

// 로그인 응답에서 Refresh Token을 쿠키로 전달
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
        // 브라우저가 인증 경로에서만 보내도록 경로 제한 / 자바스크립트에서 읽을 수 없도록 설정
        return ResponseCookie.from(NAME, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(PATH)
                .maxAge(lifetime)
                .build();
    }
}
