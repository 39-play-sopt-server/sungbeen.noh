package org.sopt.post.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import org.sopt.post.exception.InvalidPostException;

// 게시글 데이터와 검증 규칙 관리
public final class Post {

    private final long id;
    private final String title;
    private final String content;
    private final Category category;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Post(long id, String title, String content, Category category,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        // 게시글 ID와 필수 입력값 확인
        if (id <= 0) {
            throw new InvalidPostException("게시글 ID는 양수여야 합니다.");
        }
        validateText(title, "제목");
        validateText(content, "본문");
        if (category == null) {
            throw new InvalidPostException("카테고리는 필수입니다.");
        }

        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // 게시글 생성
    public static Post create(long id, String title, String content,
                              Category category, LocalDateTime now) {
        return new Post(id, title, content, category, now, now);
    }

    // 수정한 게시글을 새 객체로 반환
    public Post update(String title, String content, Category category, LocalDateTime now) {
        return new Post(id, title, content, category, createdAt, now);  //  id를 유지하고 createdAt은 그대로, updatedAt은 현재 시각으로 변경
    }

    // 제목과 내용의 입력값 확인
    private static void validateText(String value, String fieldName) {
        // 값이 없거나 공백만 입력된 경우 예외 발생
        if (value == null || value.isBlank()) {
            throw new InvalidPostException(fieldName + "은 필수입니다.");
        }
    }

    // 게시글 ID 조회
    public long getId() {
        return id;
    }

    // 게시글 제목 조회
    public String getTitle() {
        return title;
    }

    // 게시글 내용 조회
    public String getContent() {
        return content;
    }

    // 게시글 카테고리 조회
    public Category getCategory() {
        return category;
    }

    // 작성 시각 조회
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 수정 시각 조회
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
