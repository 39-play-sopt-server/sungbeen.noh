package org.sopt.common.exception;

import java.util.Objects;

// 오류 코드와 메시지를 전달하는 공통 예외
// GlobalExceptionHandler에서 도메인이 추가될 때 마다 유형을 추가하지 않고 한번에 잡기 위해 부모 클래스 생성
public class BusinessException extends RuntimeException {

    // 도메인별 Enum을 함께 받을 수 있도록 ErrorCode 인터페이스 사용
    private final ErrorCode errorCode;

    // Enum에 정의한 기본 오류 메시지 사용
    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    // 상황에 맞는 오류 메시지 사용
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
