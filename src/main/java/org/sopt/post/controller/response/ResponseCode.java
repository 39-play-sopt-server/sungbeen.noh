package org.sopt.post.controller.response;

// 처리 결과를 구분하기 위한 응답 코드
// HTTP 상태 코드와 별개, REST 전환 시 예외에 맞는 400, 404 등의 상태 지정
public enum ResponseCode {
    SUCCESS,
    INVALID_POST,
    POST_NOT_FOUND,
    POST_LIMIT_EXCEEDED,
    INTERNAL_ERROR
}
