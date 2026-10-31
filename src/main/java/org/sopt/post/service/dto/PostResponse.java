package org.sopt.post.service.dto;

import java.time.LocalDateTime;

import org.sopt.post.domain.Post;

public record PostResponse(long id, String title, String content,
                           String category, String categoryName,
                           LocalDateTime createdAt, LocalDateTime updatedAt, long viewCount,
                           Long authorId, String authorName, long likeCount) {

    // 게시글을 전달용 데이터로 변환
    public static PostResponse from(Post post) {
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(),
                post.getCategory().name(), post.getCategory().getDisplayName(),
                post.getCreatedAt(), post.getUpdatedAt(), post.getViewCount(),
                post.getAuthor().getId(), post.getAuthor().getName(), post.getLikeCount());
    }
}
