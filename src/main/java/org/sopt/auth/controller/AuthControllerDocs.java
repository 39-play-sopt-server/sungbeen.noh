package org.sopt.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.auth.service.dto.LoginRequest;
import org.sopt.auth.service.dto.LoginResponse;
import org.sopt.common.response.RestResponse;
import org.sopt.member.service.dto.MemberCreateRequest;
import org.sopt.member.service.dto.MemberResponse;
import org.springframework.http.ResponseEntity;

// Swagger 설명은 실제 요청 처리 코드와 분리
@Tag(name = "인증", description = "이메일·비밀번호 가입 및 JWT 로그인")
public interface AuthControllerDocs {

    @Operation(summary = "회원가입", description = "이름, 이메일, 비밀번호를 입력합니다. 비밀번호는 8자 이상, UTF-8 기준 72바이트 이하입니다.")
    @ApiResponse(responseCode = "201", description = "가입 성공", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "잘못된 이름·이메일·비밀번호",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(responseCode = "409", description = "이미 가입된 이메일",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    ResponseEntity<RestResponse<MemberResponse>> signup(MemberCreateRequest request);

    @Operation(summary = "로그인", description = "허용된 Origin에서 로그인하면 Access Token은 본문, Refresh Token은 HttpOnly 쿠키로 받습니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "이메일·비밀번호 형식 오류",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(responseCode = "403", description = "Origin이 없거나 허용되지 않음",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    ResponseEntity<RestResponse<LoginResponse>> login(LoginRequest request);

    @Operation(summary = "Access Token 재발급", description = "허용된 Origin에서 Refresh Token 쿠키를 보내면 새 Access Token을 받습니다. Refresh Token의 만료 시각은 유지됩니다.")
    @ApiResponse(responseCode = "200", description = "재발급 성공", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "Refresh Token 쿠키 누락, 만료 또는 검증 실패",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(responseCode = "403", description = "Origin이 없거나 허용되지 않음",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    ResponseEntity<RestResponse<LoginResponse>> refresh(@Parameter(hidden = true) String refreshToken);
}
