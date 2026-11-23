package org.sopt.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.common.response.RestResponse;
import org.sopt.member.service.dto.MemberCreateRequest;
import org.sopt.member.service.dto.MemberResponse;
import org.springframework.http.ResponseEntity;

// Swagger 문서 설명과 실제 요청 처리 코드 분리
@Tag(name = "회원", description = "회원 API")
@ApiResponse(responseCode = "500", description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = RestResponse.class)))
public interface MemberControllerDocs {

    @Operation(summary = "회원 생성", description = "이름은 255자 이하이며 이메일과 비밀번호도 필수입니다. /api/v1/auth/signup과 같은 가입 규칙을 사용합니다.")
    @ApiResponse(responseCode = "201", description = "회원 생성 성공", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "잘못된 회원 입력 또는 요청 본문",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(responseCode = "409", description = "이미 가입된 이메일",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    ResponseEntity<RestResponse<MemberResponse>> create(MemberCreateRequest request);

    @Operation(summary = "내 정보 조회")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "내 정보 조회 성공", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "유효한 인증 토큰 필요",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    ResponseEntity<RestResponse<MemberResponse>> me(@Parameter(hidden = true) Long memberId);

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "회원 삭제", description = "본인만 삭제할 수 있습니다. 작성한 게시글이 있으면 삭제를 거부하며, 회원 삭제 시 좋아요를 함께 정리합니다.")
    @ApiResponse(responseCode = "200", description = "회원 삭제 성공 및 Refresh Token 쿠키 삭제", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "404", description = "존재하지 않는 회원",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(responseCode = "409", description = "작성한 게시글이 있는 회원",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(responseCode = "401", description = "유효한 인증 토큰 필요",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(responseCode = "403", description = "본인만 탈퇴 가능",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    ResponseEntity<RestResponse<Void>> delete(@Parameter(description = "회원 ID", example = "1") long id,
                                            @Parameter(hidden = true) Long memberId);
}
