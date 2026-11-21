package org.sopt.common.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// CORS와 인증 요청의 Origin 검사에 같은 출처 목록 사용
@Component
public class BrowserOrigins {

    private final List<String> allowedOrigins;

    public BrowserOrigins(@Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins).map(String::strip).filter(value -> !value.isEmpty()).toList();
        // 쿠키를 허용하므로 명시적으로 등록한 출처만 받음
        if (this.allowedOrigins.stream().anyMatch(value -> value.contains("*") || value.equals("null"))) {
            throw new IllegalArgumentException("브라우저 요청에 허용할 출처를 정확한 주소로 설정해주세요.");
        }
    }

    public List<String> allowedOrigins() {
        return allowedOrigins;
    }

    public boolean allows(String origin) {
        return origin != null && allowedOrigins.contains(origin);
    }
}
