package org.sopt.post.exception;

import org.sopt.common.exception.BusinessException;

// 게시글 처리 중 발생한 예외
public class PostException extends BusinessException {

    // 게시글 오류 코드만 받아 기본 메시지 사용
    public PostException(PostErrorCode errorCode) {
        super(errorCode);
    }

    // 상황에 맞는 오류 메시지 사용
    public PostException(PostErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
