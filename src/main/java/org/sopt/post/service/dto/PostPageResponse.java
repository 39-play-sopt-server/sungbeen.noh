package org.sopt.post.service.dto;

import java.util.List;

// 게시글 목록과 페이지 정보 전달
public record PostPageResponse(List<PostResponse> items, int page, int size,
                               int totalElements, int totalPages) {
}
