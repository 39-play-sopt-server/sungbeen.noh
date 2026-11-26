package org.sopt.member.controller;

import java.util.Objects;

import org.sopt.auth.controller.RefreshTokenCookie;
import org.sopt.common.response.RestResponse;
import org.sopt.member.service.MemberService;
import org.sopt.member.service.dto.MemberCreateRequest;
import org.sopt.member.service.dto.MemberResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController implements MemberControllerDocs {

    private final MemberService service;
    private final RefreshTokenCookie refreshTokenCookie;

    public MemberController(MemberService service, RefreshTokenCookie refreshTokenCookie) {
        this.service = Objects.requireNonNull(service);
        this.refreshTokenCookie = Objects.requireNonNull(refreshTokenCookie);
    }

    // 회원 생성 요청
    @Override
    @PostMapping
    public ResponseEntity<RestResponse<MemberResponse>> create(@RequestBody MemberCreateRequest request) {
        MemberResponse member = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.success("회원이 생성되었습니다.", member));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<RestResponse<MemberResponse>> me(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(RestResponse.success("내 정보를 조회했습니다.", service.findById(memberId)));
    }

    // 회원 삭제 요청
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable("id") long id, @AuthenticationPrincipal Long memberId) {
        service.delete(id, memberId);
        // 회원 삭제가 끝난 뒤 쿠키 삭제 / 탈퇴가 거절되면 기존 로그인 상태 유지
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.expire().toString())
                .body(RestResponse.success("회원이 삭제되었습니다.", null));
    }
}
