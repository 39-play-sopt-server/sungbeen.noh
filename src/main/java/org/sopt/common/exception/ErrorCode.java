package org.sopt.common.exception;

import org.springframework.http.HttpStatus;

// 도메인별 오류 코드에서 공통으로 제공할 값 정의
public interface ErrorCode {

    HttpStatus getStatus();

    String getMessage();

    String getCode();
}
