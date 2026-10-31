package org.sopt.post.service.dto;

// 게시글 생성 시 작성할 회원의 ID도 함께 전달
public record PostCreateRequest(Long memberId, String title, String content, String category) {
}
