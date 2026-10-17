package org.sopt.post.exception;

import org.sopt.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

// 게시글 오류의 HTTP 상태와 기본 메시지 관리
public enum PostErrorCode implements ErrorCode {
    INVALID_POST(HttpStatus.BAD_REQUEST, "게시글 입력값이 올바르지 않습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    POST_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "게시글 작성 한도를 초과했습니다.");

    private final HttpStatus status;
    private final String message;

    PostErrorCode(HttpStatus status, String message) {
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
