package org.sopt.post.exception;

// 게시글 입력값이 잘못된 경우의 예외
public final class InvalidPostException extends PostException {

    public InvalidPostException(String message) {
        // 부모 예외에 오류 메시지 전달
        super(message);
    }
}
