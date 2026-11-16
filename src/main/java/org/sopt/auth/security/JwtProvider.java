package org.sopt.auth.security;

import java.util.Date;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 회원 ID를 담은 토큰 발급과 서명·만료 검증 담당
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final JwtParser parser;
    private final long accessTokenExpiration;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-token-expiration}") long accessTokenExpiration) {
        if (accessTokenExpiration <= 0) {
            throw new IllegalArgumentException("JWT 유효 시간은 양수여야 합니다.");
        }
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.parser = Jwts.parser().verifyWith(secretKey).build();
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public String createAccessToken(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("회원 ID는 양수여야 합니다.");
        }
        Date now = new Date();
        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public Long getMemberId(String token) {
        Claims claims = parser.parseSignedClaims(token).getPayload();
        Long memberId = Long.valueOf(claims.getSubject());
        // 서명된 토큰이라도 회원 ID와 만료 시각이 없으면 인증에 사용하지 않음
        if (memberId <= 0 || claims.getExpiration() == null) {
            throw new IllegalArgumentException("인증 토큰의 내용이 올바르지 않습니다.");
        }
        return memberId;
    }
}
