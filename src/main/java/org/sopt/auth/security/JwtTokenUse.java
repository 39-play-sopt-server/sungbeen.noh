package org.sopt.auth.security;

enum JwtTokenUse {
    ACCESS("access"),
    REFRESH("refresh");

    static final String CLAIM = "token_use";

    private final String value;

    JwtTokenUse(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
