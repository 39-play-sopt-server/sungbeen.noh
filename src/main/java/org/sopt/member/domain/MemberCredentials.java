package org.sopt.member.domain;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

import org.sopt.member.exception.MemberErrorCode;
import org.sopt.member.exception.MemberException;

// 가입과 로그인에서 같은 이메일 형식과 비밀번호 길이 사용
public final class MemberCredentials {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
                    + "@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+"
                    + "[A-Za-z]{2,63}");

    private MemberCredentials() {
    }

    // 이메일은 앞뒤 공백과 대소문자를 통일해 저장·조회
    public static String normalizeEmail(String email) {
        if (email == null) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER, "이메일은 필수입니다.");
        }
        String normalized = email.strip().toLowerCase(Locale.ROOT);
        if (normalized.length() > 254 || !EMAIL_PATTERN.matcher(normalized).matches()
                || normalized.indexOf('@') > 64) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER, "올바른 이메일 형식을 입력해주세요.");
        }
        return normalized;
    }

    // BCrypt는 UTF-8 기준 72바이트까지 처리하므로 문자 수와 바이트 수를 각각 확인
    public static void validatePassword(String password) {
        if (password == null || password.isBlank() || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER,
                    "비밀번호는 8자 이상, UTF-8 기준 72바이트 이하여야 합니다.");
        }
    }
}
