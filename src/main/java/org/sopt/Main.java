package org.sopt;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 엔티티 저장 시 생성일을 자동으로 설정하도록 Auditing 활성화
@EnableJpaAuditing
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}