package org.sopt.auth.service.dto;

// 로그인 처리 결과 / 컨트롤러에서 응답 본문과 쿠키로 나눠 전달
public record LoginTokens(String accessToken, String refreshToken) {
}
