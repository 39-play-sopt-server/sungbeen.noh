package org.sopt.post.service.dto;

import java.util.List;

import org.sopt.post.domain.Post;
import org.springframework.data.domain.Page;

// 게시글 목록과 페이지 정보 전달
public record PostPageResponse(List<PostResponse> items, int page, int size,
                               long totalElements, int totalPages) {

    // DB에서 조회한 페이지와 게시글을 기존 응답 형식으로 변환
    public static PostPageResponse from(Page<Post> posts) {
        List<PostResponse> items = posts.getContent().stream()
                .map(PostResponse::from)
                .toList();
        return new PostPageResponse(items, posts.getNumber(), posts.getSize(),
                posts.getTotalElements(), posts.getTotalPages());
    }
}
