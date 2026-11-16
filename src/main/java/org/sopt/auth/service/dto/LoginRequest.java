package org.sopt.auth.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(String email,
                           @Schema(accessMode = Schema.AccessMode.WRITE_ONLY) String password) {
}
