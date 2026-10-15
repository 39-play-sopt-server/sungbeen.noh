package org.sopt.post.domain;

import org.sopt.post.exception.PostErrorCode;
import org.sopt.post.exception.PostException;

// 정해진 카테고리만 사용할 수 있도록 enum으로 관리
public enum Category {
    FREE("자유"),
    QUESTION("질문"),
    NOTICE("공지");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    // 카테고리 이름 조회
    public String getDisplayName() {
        return displayName;
    }

    // 입력받은 코드로 카테고리 선택
    public static Category from(String code) {
        if (code == null || code.isBlank()) {
            // 기본 메세지는 있지만 더 명확한 메세지 전달을 위해 직접 메세지 작성하는 생성자 사용
            throw new PostException(PostErrorCode.INVALID_POST, "카테고리는 필수입니다.");
        }

        // 앞뒤 공백 제거 후 대문자로 변환
        try {
            return valueOf(code.strip().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new PostException(PostErrorCode.INVALID_POST, "존재하지 않는 카테고리입니다.");
        }
    }
}
