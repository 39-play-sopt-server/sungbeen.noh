package org.sopt.member.exception;

import org.sopt.common.exception.BusinessException;

// 회원 처리 중 발생한 예외 / 공통 예외 처리에서 함께 처리
public class MemberException extends BusinessException {

    public MemberException(MemberErrorCode errorCode) {
        super(errorCode);
    }

    public MemberException(MemberErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
