package org.sopt.post;

import java.util.Objects;

public final class Post {
    private String title;
    private String content;

    private Post(String title, String content) {
        this.title = Objects.requireNonNull(title, "게시글 제목은 null 일 수 없습니다.");
        this.content = Objects.requireNonNull(content, "게시글 내용은 null 일 수 없습니다.");
    }

    // 게시글 생성
    public static Post create(String title, String content) {   // 정적 팩토리 메서드 패턴
        return new Post(title, content);
    }

    // 게시글 제목 조회
    public String getTitle() {
        return this.title;
    }

    // 게시글 내용 조회
    public String getContent() {
        return this.content;
    }

    // 게시글 수정
    public void update(String title, String content) {
        this.title = Objects.requireNonNull(title, "게시글 제목은 null 일 수 없습니다.");
        this.content = Objects.requireNonNull(content, "게시글 내용은 null 일 수 없습니다.");
    }
}