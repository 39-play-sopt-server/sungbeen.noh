package org.sopt.post.controller.response;

import java.util.Objects;

// 콘솔에서 사용하는 공통 응답
// 현재는 메서드 반환값으로 전달 / HTTP 응답 전송 기능은 없는 데이터 객체
// REST 전환 시 HTTP 상태와 JSON으로 변환할 응답 본문을 별도로 구성
// 게시글과 목록 등 여러 데이터 타입을 담을 수 있도록 T 사용
public final class ConsoleResponse<T> {

    // 응답 생성 후 값이 바뀌지 않도록 final 지정
    private final ResponseCode code;
    private final String message;
    private final T data;

    // success와 failure 메서드로만 생성하도록 생성자를 private으로 제한
    private ConsoleResponse(ResponseCode code, String message, T data) {
        this.code = Objects.requireNonNull(code);
        this.message = Objects.requireNonNull(message);
        this.data = data;
    }

    // 정적 스태틱 메서드로 성공과 실패 응답을 생성하도록 구현

    // 성공 응답 생성
    public static <T> ConsoleResponse<T> success(String message, T data) {
        return new ConsoleResponse<>(ResponseCode.SUCCESS, message, data);
    }

    // 실패 응답 생성
    public static <T> ConsoleResponse<T> failure(ResponseCode code, String message) {
        if (code == ResponseCode.SUCCESS) {
            throw new IllegalArgumentException("ResponseCode.SUCCESS는 실패 응답에 사용할 수 없습니다.");
        }
        return new ConsoleResponse<>(code, message, null);
    }

    // 응답 코드로 성공 여부 확인
    public boolean success() {
        return code == ResponseCode.SUCCESS;
    }

    // 응답 메시지 조회
    public String message() {
        return message;
    }

    // 응답 데이터 조회
    public T data() {
        return data;
    }
}
