package org.sopt.auth.security;

import java.util.Date;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 회원 ID를 담은 Access Token과 Refresh Token 발급
@Component
public class JwtTokenIssuer {

    private final SecretKey signingKey;
    // 두 유효기간은 발급 시각에 더할 값 (밀리초)
    private final long accessTokenLifetimeMillis;
    private final long refreshTokenLifetimeMillis;

    public JwtTokenIssuer(SecretKey signingKey,
                          @Value("${jwt.access-token-expiration}") long accessTokenLifetimeMillis,
                          @Value("${jwt.refresh-token-expiration}") long refreshTokenLifetimeMillis) {
        // 유효기간 설정이 잘못되면 토큰을 발급하기 전에 서버 시작 시점에 확인
        if (accessTokenLifetimeMillis <= 0 || refreshTokenLifetimeMillis <= 0) {
            throw new IllegalArgumentException("JWT 유효 시간은 양수여야 합니다.");
        }
        this.signingKey = signingKey;
        this.accessTokenLifetimeMillis = accessTokenLifetimeMillis;
        this.refreshTokenLifetimeMillis = refreshTokenLifetimeMillis;
    }

    public String issueAccessToken(long memberId) {
        return issue(memberId, JwtTokenUse.ACCESS, accessTokenLifetimeMillis);
    }

    public String issueRefreshToken(long memberId) {
        return issue(memberId, JwtTokenUse.REFRESH, refreshTokenLifetimeMillis);
    }

    private String issue(long memberId, JwtTokenUse tokenUse, long lifetimeMillis) {
        // 검증 단계에서 거부하는 0과 음수 ID는 발급 전에 차단
        if (memberId <= 0) {
            throw new IllegalArgumentException("회원 ID는 양수여야 합니다.");
        }

        // 발급 시각을 한 번 구하고 설정된 유효기간을 더해 만료 시각 계산
        Date now = new Date();
        // JWT의 sub는 문자열이라 회원 ID를 문자열로 저장
        return Jwts.builder()
                .subject(Long.toString(memberId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + lifetimeMillis))
                .claim(JwtTokenUse.CLAIM, tokenUse.value())
                .signWith(signingKey)
                .compact();
    }
}
