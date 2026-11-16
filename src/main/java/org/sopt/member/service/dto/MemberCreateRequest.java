package org.sopt.member.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberCreateRequest(String name, String email,
                                  @Schema(accessMode = Schema.AccessMode.WRITE_ONLY) String password) {
}
