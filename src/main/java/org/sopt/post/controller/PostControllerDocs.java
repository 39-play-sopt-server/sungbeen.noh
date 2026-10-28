package org.sopt.post.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.common.response.RestResponse;
import org.sopt.post.service.dto.CategoryResponse;
import org.sopt.post.service.dto.PostPageResponse;
import org.sopt.post.service.dto.PostCreateRequest;
import org.sopt.post.service.dto.PostRequest;
import org.sopt.post.service.dto.PostResponse;
import org.springframework.http.ResponseEntity;

// Swagger 문서 설명과 실제 요청 처리 코드 분리
@Tag(name = "게시글", description = "게시글 API")
@ApiResponse(
        responseCode = "500", description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = RestResponse.class)))
public interface PostControllerDocs {

    @Operation(summary = "게시글 작성",
            description = "작성자 회원 ID, 제목·본문은 필수이며, category는 FREE, QUESTION, NOTICE 중 하나입니다. 최초 조회수는 0입니다.")
    @ApiResponse(
            responseCode = "201", description = "작성 성공", useReturnTypeSchema = true)
    @ApiResponse(
            responseCode = "400", description = "잘못된 게시글 입력 또는 요청 본문",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(
            responseCode = "409", description = "최대 게시글 수 초과",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(
            responseCode = "404", description = "존재하지 않는 작성자",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    ResponseEntity<RestResponse<PostResponse>> create(PostCreateRequest request);

    @Operation(summary = "게시글 목록 조회",
            description = "카테고리 필터 후 ID 오름차순으로 페이지를 반환합니다. 목록 조회는 조회수를 올리지 않습니다. 범위를 벗어난 페이지는 빈 목록입니다.")
    @ApiResponse(
            responseCode = "200", description = "목록 조회 성공", useReturnTypeSchema = true)
    @ApiResponse(
            responseCode = "400", description = "잘못된 페이지·크기·카테고리",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    ResponseEntity<RestResponse<PostPageResponse>> findAll(
            @Parameter(description = "0부터 시작하는 페이지 번호, 기본값 0", example = "0") int page,
            @Parameter(description = "페이지당 개수, 1~100, 기본값 10", example = "10") int size,
            @Parameter(description = "FREE, QUESTION, NOTICE 중 선택, 생략 시 전체 조회", example = "QUESTION")
            String category);

    @Operation(summary = "게시글 상세 조회",
            description = "상세 조회마다 조회수가 1 증가합니다. 작성·수정 시각은 유지됩니다.")
    @ApiResponse(
            responseCode = "200", description = "상세 조회 성공", useReturnTypeSchema = true)
    @ApiResponse(
            responseCode = "400", description = "숫자가 아닌 게시글 ID",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(
            responseCode = "404", description = "존재하지 않는 게시글",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    ResponseEntity<RestResponse<PostResponse>> findById(
            @Parameter(description = "게시글 ID", example = "1") long id);

    @Operation(summary = "게시글 수정",
            description = "제목·본문·카테고리를 수정하며, 기존 조회수와 작성 시각을 유지합니다.")
    @ApiResponse(
            responseCode = "200", description = "수정 성공", useReturnTypeSchema = true)
    @ApiResponse(
            responseCode = "400", description = "잘못된 ID·게시글 입력 또는 요청 본문",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(
            responseCode = "404", description = "존재하지 않는 게시글",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    ResponseEntity<RestResponse<PostResponse>> update(
            @Parameter(description = "게시글 ID", example = "1") long id, PostRequest request);

    @Operation(summary = "게시글 삭제", description = "삭제 성공 시 공통 응답의 data는 null입니다.")
    @ApiResponse(
            responseCode = "200", description = "삭제 성공", useReturnTypeSchema = true)
    @ApiResponse(
            responseCode = "400", description = "숫자가 아닌 게시글 ID",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    @ApiResponse(
            responseCode = "404", description = "존재하지 않는 게시글",
            content = @Content(schema = @Schema(implementation = RestResponse.class)))
    ResponseEntity<RestResponse<Void>> delete(
            @Parameter(description = "게시글 ID", example = "1") long id);

    @Operation(summary = "카테고리 목록 조회", description = "선택 가능한 카테고리 코드와 이름을 반환합니다.")
    @ApiResponse(
            responseCode = "200", description = "카테고리 조회 성공", useReturnTypeSchema = true)
    ResponseEntity<RestResponse<List<CategoryResponse>>> getCategories();
}
