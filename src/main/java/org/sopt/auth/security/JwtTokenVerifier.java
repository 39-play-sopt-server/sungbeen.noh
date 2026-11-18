package org.sopt.auth.security;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

// 서명과 만료를 검증한 뒤 회원 ID 반환
@Component
public class JwtTokenVerifier {

    private final JwtParser parser;

    public JwtTokenVerifier(SecretKey signingKey) {
        // 요청마다 파서를 새로 만들지 않도록 서명 검증에 사용할 키를 미리 설정
        this.parser = Jwts.parser().verifyWith(signingKey).build();
    }

    public long verifyAccessToken(String token) {
        // 서명과 토큰에 들어 있는 만료 시각을 검증한 뒤 claim을 읽음
        Claims claims = parser.parseSignedClaims(token).getPayload();
        long memberId = Long.parseLong(claims.getSubject());
        // 파서는 exp가 없는 토큰도 읽을 수 있어 만료 시각이 들어 있는지 별도로 확인
        if (memberId <= 0 || claims.getExpiration() == null) {
            throw new IllegalArgumentException("인증 토큰의 내용이 올바르지 않습니다.");
        }
        return memberId;
    }
}
