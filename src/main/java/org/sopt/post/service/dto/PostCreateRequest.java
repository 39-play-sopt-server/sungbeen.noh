package org.sopt.post.service.dto;

// 작성자는 요청 본문 대신 인증 토큰에서 결정
public record PostCreateRequest(String title, String content, String category) {
}
