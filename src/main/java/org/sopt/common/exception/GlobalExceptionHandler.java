package org.sopt.common.exception;

import org.sopt.common.response.RestResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// Controller에서 발생한 예외를 공통 응답으로 반환
@RestControllerAdvice
public class GlobalExceptionHandler {

    // PostException 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<RestResponse<Void>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        RestResponse<Void> response = RestResponse.failure(errorCode.getCode(), exception.getMessage());

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 입력 오류와 그 외의 예상하지 못 한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Void>> handleException(Exception exception) {
        // 잘못된 JSON, 본문 누락, 숫자 변환 실패 등의 입력 오류 처리
        if (exception instanceof HttpMessageNotReadableException
                || exception instanceof MethodArgumentTypeMismatchException
                || exception instanceof ServletRequestBindingException) {
            ErrorCode errorCode = CommonErrorCode.INVALID_REQUEST;
            return ResponseEntity.status(errorCode.getStatus())
                    .body(RestResponse.failure(errorCode.getCode(), errorCode.getMessage()));
        }

        // ErrorResponse: HTTP 상태와 헤더를 제공하는 Spring 오류의 공통 인터페이스
        // instanceof로 확인 후 형변환 / 요청 오류를 무조건 500으로 바꾸는 처리 방지
        if (exception instanceof ErrorResponse) {
            ErrorResponse errorResponse = (ErrorResponse) exception;
            // HTTP 상태를 사용한 문자열 코드 생성 / 오류 안내는 JSON으로 전달
            return ResponseEntity.status(errorResponse.getStatusCode()).headers(errorResponse.getHeaders())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(RestResponse.failure("HTTP_" + errorResponse.getStatusCode().value(),
                            "요청을 처리할 수 없습니다."));
        }

        // 내부 예외 메시지를 응답에 노출하지 않고 공통 안내 반환
        ErrorCode errorCode = CommonErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(RestResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }
}
