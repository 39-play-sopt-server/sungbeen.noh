package org.sopt.common.response;

import java.util.Objects;

// API 성공과 실패에 공통으로 사용하는 응답 / 성공, 실패 응답을 다르게 하고싶으면 인터페이스로 두고 SuccessResponse, ErrorResponse를 나눠도 됨
public final class RestResponse<T> {

    // 응답 생성 후 값이 바뀌지 않도록 final 지정
    private final String code;
    private final String message;
    private final T data;

    // success와 failure 메서드로만 생성하도록 생성자를 private으로 막음
    private RestResponse(String code, String message, T data) {
        this.code = Objects.requireNonNull(code);
        this.message = Objects.requireNonNull(message);
        this.data = data;
    }

    // 성공 응답 생성
    public static <T> RestResponse<T> success(String message, T data) {
        return new RestResponse<>("SUCCESS", message, data);
    }

    // 실패 응답 생성
    public static RestResponse<Void> failure(String code, String message) {
        if ("SUCCESS".equals(code)) {
            throw new IllegalArgumentException("SUCCESS는 실패 응답에 사용할 수 없습니다.");
        }
        return new RestResponse<>(code, message, null);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
