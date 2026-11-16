package org.sopt.auth.controller;

import java.util.Objects;

import org.sopt.auth.service.AuthService;
import org.sopt.auth.service.dto.LoginRequest;
import org.sopt.auth.service.dto.LoginResponse;
import org.sopt.common.response.RestResponse;
import org.sopt.member.service.MemberService;
import org.sopt.member.service.dto.MemberCreateRequest;
import org.sopt.member.service.dto.MemberResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;
    private final MemberService memberService;

    public AuthController(AuthService authService, MemberService memberService) {
        this.authService = Objects.requireNonNull(authService);
        this.memberService = Objects.requireNonNull(memberService);
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
        return ResponseEntity.ok(RestResponse.success("로그인되었습니다.", authService.login(request)));
    }
}
