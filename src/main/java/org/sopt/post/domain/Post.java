package org.sopt.post.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.sopt.post.exception.PostErrorCode;
import org.sopt.post.exception.PostException;

// 게시글 데이터와 검증 규칙 관리 / JPA 테이블 매핑
@Entity
@Table(name = "posts")
public class Post {

    // 저장 전에는 null, 저장 후에는 저장소에서 발급한 ID 사용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기존 입력 규칙을 유지하도록 긴 문자열을 저장할 수 있는 MySQL 타입 지정
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    // Enum 순서 변경에 영향을 받지 않도록 이름으로 저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private long viewCount;

    // JPA에서 객체 생성과 필드 설정이 가능하도록 기본 생성자와 변경 가능한 필드 사용
    protected Post() {
    }

    private Post(Long id, String title, String content, Category category,
                 LocalDateTime createdAt, LocalDateTime updatedAt, long viewCount) {
        // 게시글 ID와 필수 입력값 확인
        if (id != null && id <= 0) {
            throw new PostException(PostErrorCode.INVALID_POST, "게시글 ID는 양수여야 합니다.");
        }
        validateText(title, "제목");
        validateText(content, "본문");
        if (category == null) {
            throw new PostException(PostErrorCode.INVALID_POST, "카테고리는 필수입니다.");
        }

        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.viewCount = viewCount;
    }

    // 게시글 생성
    public static Post create(String title, String content,
                              Category category, LocalDateTime now) {
        return new Post(null, title, content, category, now, now, 0);
    }

    // 수정한 게시글을 새 객체로 반환
    public Post update(String title, String content, Category category, LocalDateTime now) {
        return new Post(id, title, content, category, createdAt, now, viewCount);
    }

    // 작성·수정 시각을 유지하고 조회수가 증가한 게시글 반환
    public Post increaseViewCount() {
        return new Post(id, title, content, category, createdAt, updatedAt, viewCount + 1);
    }

    // 제목과 내용의 입력값 확인
    private static void validateText(String value, String fieldName) {
        // 값이 없거나 공백만 입력된 경우 예외 발생
        if (value == null || value.isBlank()) {
            throw new PostException(PostErrorCode.INVALID_POST, fieldName + "은 필수입니다.");
        }
    }

    // 게시글 ID 조회
    public Long getId() {
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

    // 조회수 확인
    public long getViewCount() {
        return viewCount;
    }
}
