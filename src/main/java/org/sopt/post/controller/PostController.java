package org.sopt.post.controller;

import java.util.List;
import java.util.Objects;

import org.sopt.common.response.RestResponse;
import org.sopt.post.service.PostService;
import org.sopt.post.service.dto.CategoryResponse;
import org.sopt.post.service.dto.PostRequest;
import org.sopt.post.service.dto.PostResponse;
import org.sopt.post.service.dto.PostPageResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/api/v1/posts")
public final class PostController implements PostControllerDocs {

    // 전달받은 Service를 다른 객체로 바꾸지 못하도록 final 지정
    private final PostService service;

    // Controller가 Service를 직접 생성하지 않고 외부에서 전달받음
    public PostController(PostService service) {
        this.service = Objects.requireNonNull(service);
    }

    // 게시글 작성 요청
    @Override
    @PostMapping
    public ResponseEntity<RestResponse<PostResponse>> create(@RequestBody PostRequest request) {
        PostResponse post = service.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.success("게시글이 작성되었습니다.", post));
    }

    // 게시글 목록 조회 요청
    @Override
    @GetMapping
    public ResponseEntity<RestResponse<PostPageResponse>> findAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "category", required = false) String category) {
        // 게시글 목록 조회 후 성공 응답 생성
        PostPageResponse posts = service.findAll(page, size, category);
        return ResponseEntity.ok(RestResponse.success("게시글 목록을 조회했습니다.", posts));
    }

    // 게시글 단건 조회 요청
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<PostResponse>> findById(@PathVariable("id") long id) {
        PostResponse post = service.findById(id);
        return ResponseEntity.ok(RestResponse.success("게시글을 조회했습니다.", post));
    }

    // 게시글 수정 요청
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<PostResponse>> update(@PathVariable("id") long id,
                                                            @RequestBody PostRequest request) {
        // 게시글 수정 후 성공 응답 생성
        PostResponse post = service.update(id, request);
        return ResponseEntity.ok(RestResponse.success("게시글이 수정되었습니다.", post));
    }

    // 게시글 삭제 요청
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable("id") long id) {
        service.delete(id);
        return ResponseEntity.ok(RestResponse.success("게시글이 삭제되었습니다.", null));
    }

    // 카테고리 목록 조회 요청
    @Override
    @GetMapping("/categories")
    public ResponseEntity<RestResponse<List<CategoryResponse>>> getCategories() {
        // 카테고리 목록 조회 후 성공 응답 생성
        List<CategoryResponse> categories = service.getCategories();
        return ResponseEntity.ok(RestResponse.success("카테고리 목록을 조회했습니다.", categories));
    }
}
