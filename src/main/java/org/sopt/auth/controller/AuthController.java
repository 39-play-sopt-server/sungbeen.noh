package org.sopt.auth.controller;

import java.util.Objects;

import org.sopt.auth.service.AuthService;
import org.sopt.auth.service.dto.LoginRequest;
import org.sopt.auth.service.dto.LoginResponse;
import org.sopt.auth.service.dto.LoginTokens;
import org.sopt.common.response.RestResponse;
import org.sopt.member.service.MemberService;
import org.sopt.member.service.dto.MemberCreateRequest;
import org.sopt.member.service.dto.MemberResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;
    private final MemberService memberService;
    private final RefreshTokenCookie refreshTokenCookie;

    public AuthController(AuthService authService, MemberService memberService, RefreshTokenCookie refreshTokenCookie) {
        this.authService = Objects.requireNonNull(authService);
        this.memberService = Objects.requireNonNull(memberService);
        this.refreshTokenCookie = Objects.requireNonNull(refreshTokenCookie);
    }

    // 기존 회원 생성과 같은 서비스로 가입 규칙을 한 곳에서 관리
    @Override
    @PostMapping("/signup")
    public ResponseEntity<RestResponse<MemberResponse>> signup(@RequestBody MemberCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.success("회원이 생성되었습니다.", memberService.create(request)));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<RestResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginTokens tokens = authService.login(request);
        // 응답 본문에는 Access Token만 담고 Refresh Token은 쿠키로 전달
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(tokens.refreshToken()).toString())
                .body(RestResponse.success("로그인되었습니다.", new LoginResponse(tokens.accessToken())));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<RestResponse<LoginResponse>> refresh(
            @CookieValue(name = RefreshTokenCookie.NAME, required = false) String refreshToken) {
        // 쿠키가 없을 때도 서비스에서 같은 재발급 오류로 처리
        String accessToken = authService.refresh(refreshToken);
        return ResponseEntity.ok(RestResponse.success("Access Token이 재발급되었습니다.", new LoginResponse(accessToken)));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<RestResponse<Void>> logout() {
        // 토큰이 만료됐거나 쿠키가 이미 없어도 브라우저에 삭제 응답 전달
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.expire().toString())
                .body(RestResponse.success("로그아웃되었습니다.", null));
    }
}
