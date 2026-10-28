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
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.sopt.common.domain.BaseEntity;
import org.sopt.member.domain.Member;
import org.sopt.post.exception.PostErrorCode;
import org.sopt.post.exception.PostException;

// 게시글 데이터와 검증 규칙 관리 / JPA 테이블 매핑
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

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

    // 여러 게시글이 한 명의 회원을 작성자로 참조 / 필요할 때 작성자 조회
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private long viewCount;

    // JPA에서 객체 생성과 필드 설정이 가능하도록 기본 생성자와 변경 가능한 필드 사용
    protected Post() {
    }

    private Post(String title, String content, Category category,
                 Member author, LocalDateTime now) {
        // 게시글 필수 입력값 확인
        validateText(title, "제목");
        validateText(content, "본문");
        if (category == null) {
            throw new PostException(PostErrorCode.INVALID_POST, "카테고리는 필수입니다.");
        }

        this.title = title;
        this.content = content;
        this.category = category;
        this.author = Objects.requireNonNull(author);
        this.updatedAt = Objects.requireNonNull(now);
        this.viewCount = 0;
    }

    // 게시글 생성
    public static Post create(String title, String content,
                              Category category, Member author, LocalDateTime now) {
        return new Post(title, content, category, author, now);
    }

    // 입력값을 검증한 뒤 기존 게시글의 내용과 수정 시각 변경
    public void update(String title, String content, Category category, LocalDateTime now) {
        validateText(title, "제목");
        validateText(content, "본문");
        if (category == null) {
            throw new PostException(PostErrorCode.INVALID_POST, "카테고리는 필수입니다.");
        }
        Objects.requireNonNull(now);

        this.title = title;
        this.content = content;
        this.category = category;
        this.updatedAt = now;
    }

    // 작성·수정 시각은 유지하고 조회수만 증가
    public void increaseViewCount() {
        this.viewCount++;
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

    // 작성자 조회
    public Member getAuthor() {
        return author;
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
