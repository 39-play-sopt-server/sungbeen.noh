package org.sopt.member.controller;

import java.util.Objects;

import org.sopt.common.response.RestResponse;
import org.sopt.member.service.MemberService;
import org.sopt.member.service.dto.MemberCreateRequest;
import org.sopt.member.service.dto.MemberResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController implements MemberControllerDocs {

    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = Objects.requireNonNull(service);
    }

    // 회원 생성 요청
    @Override
    @PostMapping
    public ResponseEntity<RestResponse<MemberResponse>> create(@RequestBody MemberCreateRequest request) {
        MemberResponse member = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.success("회원이 생성되었습니다.", member));
    }

    // 회원 삭제 요청
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable("id") long id) {
        service.delete(id);
        return ResponseEntity.ok(RestResponse.success("회원이 삭제되었습니다.", null));
    }
}
