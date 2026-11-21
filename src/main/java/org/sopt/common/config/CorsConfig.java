package org.sopt.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final BrowserOrigins browserOrigins;

    public CorsConfig(BrowserOrigins browserOrigins) {
        this.browserOrigins = browserOrigins;
    }

    // 설정된 출처의 브라우저 요청만 API에 허용
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(browserOrigins.allowedOrigins().toArray(String[]::new))
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
