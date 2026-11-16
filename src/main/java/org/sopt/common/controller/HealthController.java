package org.sopt.common.controller;

import org.sopt.common.response.RestResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 배포 확인에서 인증이 필요한 업무 API 대신 사용
@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/v1/health")
    public RestResponse<Void> health() {
        // 기존 게시글 조회 기반 배포 확인처럼 DB 연결까지 확인
        jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return RestResponse.success("서버가 실행 중입니다.", null);
    }
}
