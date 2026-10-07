package org.sopt.post.exception;

// 게시글 예외의  공통 부모 클래스
public abstract class PostException extends RuntimeException {

    // 자식 예외에서 생성자를 사용할 수 있도록 protected 지정
    protected PostException(String message) {
        // 호출한 곳에서 처리할 수 있도록 오류 메시지 전달
        super(message);
    }
}
