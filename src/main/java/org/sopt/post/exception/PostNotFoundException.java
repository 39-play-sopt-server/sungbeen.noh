package org.sopt.post.exception;

// 존재하지 않는 게시글을 요청한 경우의 예외
public final class PostNotFoundException extends PostException {

    public PostNotFoundException(long id) {
        // 부모 예외에 오류 메시지 전달
        super("존재하지 않는 게시글입니다. (ID: " + id + ")");
    }
}
