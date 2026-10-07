package org.sopt.post.controller;

import java.util.List;
import java.util.Objects;

import org.sopt.post.controller.response.ConsoleResponse;
import org.sopt.post.controller.response.ResponseCode;
import org.sopt.post.exception.InvalidPostException;
import org.sopt.post.exception.PostLimitExceededException;
import org.sopt.post.exception.PostNotFoundException;
import org.sopt.post.service.PostService;
import org.sopt.post.service.dto.CategoryResponse;
import org.sopt.post.service.dto.PostRequest;
import org.sopt.post.service.dto.PostResponse;

// main에서 게시글 요청을 받아 처리 결과 반환
// Spring REST 전환 시 @RestController와 요청 경로 매핑으로 HTTP 요청 수신
// 현재는 각 메서드에서 업무 예외 처리
// Spring REST 전환 시 GlobalExceptionHandler에 @RestControllerAdvice를 붙여 공통 처리, 지금은 지저분해도 참으세요 ㅎㅎ
public final class PostController {

    // 전달받은 Service를 다른 객체로 바꾸지 못하도록 final 지정
    private final PostService service;

    // Controller가 Service를 직접 생성하지 않고 외부에서 전달받음
    // 현재는 Main에서 전달, Spring 전환 시 등록된 Service를 컨테이너에서 주입
    public PostController(PostService service) {
        this.service = Objects.requireNonNull(service);
    }

    // 게시글 작성 요청
    public ConsoleResponse<PostResponse> create(PostRequest request) {
        try {
            // 게시글 작성 후 성공 응답 생성
            PostResponse post = service.create(request);
            return ConsoleResponse.success("게시글이 작성되었습니다.", post);
        } catch (InvalidPostException exception) {
            // 입력값 오류 응답 생성
            return ConsoleResponse.failure(ResponseCode.INVALID_POST, exception.getMessage());
        } catch (PostLimitExceededException exception) {
            // 작성 한도 초과 응답 생성
            return ConsoleResponse.failure(ResponseCode.POST_LIMIT_EXCEEDED, exception.getMessage());
        }
    }

    // 게시글 목록 조회 요청
    public ConsoleResponse<List<PostResponse>> findAll() {
        // 게시글 목록 조회 후 성공 응답 생성
        List<PostResponse> posts = service.findAll();
        return ConsoleResponse.success("게시글 목록을 조회했습니다.", posts);
    }

    // 게시글 단건 조회 요청
    public ConsoleResponse<PostResponse> findById(long id) {
        try {
            // 게시글 조회 후 성공 응답 생성
            PostResponse post = service.findById(id);
            return ConsoleResponse.success("게시글을 조회했습니다.", post);
        } catch (PostNotFoundException exception) {
            // 존재하지 않는 게시글의 실패 응답 생성
            return ConsoleResponse.failure(ResponseCode.POST_NOT_FOUND, exception.getMessage());
        }
    }

    // 게시글 수정 요청
    public ConsoleResponse<PostResponse> update(long id, PostRequest request) {
        try {
            // 게시글 수정 후 성공 응답 생성
            PostResponse post = service.update(id, request);
            return ConsoleResponse.success("게시글이 수정되었습니다.", post);
        } catch (InvalidPostException exception) {
            // 입력값 오류 응답 생성
            return ConsoleResponse.failure(ResponseCode.INVALID_POST, exception.getMessage());
        } catch (PostNotFoundException exception) {
            // 존재하지 않는 게시글의 실패 응답 생성
            return ConsoleResponse.failure(ResponseCode.POST_NOT_FOUND, exception.getMessage());
        }
    }

    // 게시글 삭제 요청
    public ConsoleResponse<Void> delete(long id) {
        try {
            // 게시글 삭제 후 성공 응답 생성
            service.delete(id);
            return ConsoleResponse.success("게시글이 삭제되었습니다.", null);
        } catch (PostNotFoundException exception) {
            // 존재하지 않는 게시글의 실패 응답 생성
            return ConsoleResponse.failure(ResponseCode.POST_NOT_FOUND, exception.getMessage());
        }
    }

    // 카테고리 목록 조회 요청
    public ConsoleResponse<List<CategoryResponse>> getCategories() {
        // 카테고리 목록 조회 후 성공 응답 생성
        List<CategoryResponse> categories = service.getCategories();
        return ConsoleResponse.success("카테고리 목록을 조회했습니다.", categories);
    }
}
