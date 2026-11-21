package org.sopt.auth.service;

import java.util.Objects;

import io.jsonwebtoken.JwtException;
import org.sopt.auth.exception.AuthErrorCode;
import org.sopt.auth.security.JwtTokenIssuer;
import org.sopt.auth.security.JwtTokenVerifier;
import org.sopt.auth.service.dto.LoginRequest;
import org.sopt.auth.service.dto.LoginTokens;
import org.sopt.common.exception.BusinessException;
import org.sopt.member.domain.Member;
import org.sopt.member.domain.MemberCredentials;
import org.sopt.member.exception.MemberErrorCode;
import org.sopt.member.exception.MemberException;
import org.sopt.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenIssuer jwtTokenIssuer;
    private final JwtTokenVerifier jwtTokenVerifier;
    private final String dummyPassword;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                       JwtTokenIssuer jwtTokenIssuer, JwtTokenVerifier jwtTokenVerifier) {
        this.memberRepository = Objects.requireNonNull(memberRepository);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
        this.jwtTokenIssuer = Objects.requireNonNull(jwtTokenIssuer);
        this.jwtTokenVerifier = Objects.requireNonNull(jwtTokenVerifier);
        // 없는 이메일도 해시 비교를 수행해 계정 유무에 따른 처리 시간 차이를 줄임
        this.dummyPassword = passwordEncoder.encode("unregistered-member-password");
    }

    @Transactional(readOnly = true)
    public LoginTokens login(LoginRequest request) {
        if (request == null) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER);
        }
        String email = MemberCredentials.normalizeEmail(request.email());
        MemberCredentials.validatePassword(request.password());
        Member member = memberRepository.findByEmail(email).orElse(null);
        String encodedPassword = member == null || member.getPassword() == null
                ? dummyPassword : member.getPassword();
        boolean matches = passwordEncoder.matches(request.password(), encodedPassword);
        // 이메일 미가입과 비밀번호 불일치는 같은 오류로 응답
        if (member == null || member.getPassword() == null || !matches) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        return new LoginTokens(jwtTokenIssuer.issueAccessToken(member.getId()),
                jwtTokenIssuer.issueRefreshToken(member.getId()));
    }

    @Transactional(readOnly = true)
    public String refresh(String refreshToken) {
        long memberId;
        try {
            memberId = jwtTokenVerifier.verifyRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            // 쿠키 누락, 토큰 만료, 잘못된 토큰 모두 다시 로그인 안내
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 만료 전이라도 탈퇴한 회원에게는 새 토큰을 발급하지 않음
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        // 최초 로그인 때 정한 Refresh Token 만료 시각을 유지
        return jwtTokenIssuer.issueAccessToken(memberId);
    }
}
