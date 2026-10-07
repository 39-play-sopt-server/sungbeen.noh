package org.sopt.post.domain;

import org.sopt.post.exception.InvalidPostException;

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
            throw new InvalidPostException("카테고리는 필수입니다.");
        }

        // 앞뒤 공백 제거 후 대문자로 변환
        try {
            return valueOf(code.strip().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidPostException("존재하지 않는 카테고리입니다.");
        }
    }
}
