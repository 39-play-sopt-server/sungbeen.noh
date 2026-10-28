package org.sopt.member.exception;

import org.sopt.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

// 회원 오류의 HTTP 상태와 기본 메시지 관리
public enum MemberErrorCode implements ErrorCode {
    INVALID_MEMBER(HttpStatus.BAD_REQUEST, "회원 입력값이 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    MEMBER_HAS_POSTS(HttpStatus.CONFLICT, "작성한 게시글이 있는 회원은 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    MemberErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getCode() {
        return name();
    }
}
