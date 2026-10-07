package org.sopt.post.exception;

// 최대 게시글 수를 넘긴 경우의 예외
public final class PostLimitExceededException extends PostException {

    public PostLimitExceededException(int limit) {
        // 부모 예외에 오류 메시지 전달
        super("게시글은 최대 " + limit + "개까지 작성할 수 있습니다.");
    }
}
