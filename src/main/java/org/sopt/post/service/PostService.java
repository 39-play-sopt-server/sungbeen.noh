package org.sopt.post.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.sopt.post.domain.Category;
import org.sopt.post.domain.Post;
import org.sopt.post.exception.InvalidPostException;
import org.sopt.post.exception.PostLimitExceededException;
import org.sopt.post.exception.PostNotFoundException;
import org.sopt.post.service.dto.CategoryResponse;
import org.sopt.post.service.dto.PostRequest;
import org.sopt.post.service.dto.PostResponse;
import org.sopt.post.service.port.PostRepository;

// 의존하고 있는 객체를 추상화 하여 PostService가 PostRepository 구현체에 직접 의존하지 않도록 설계
public final class PostService {

    // 모든 Service 객체에서 같은 작성 한도를 사용하도록 static final 지정
    private static final int MAX_POST_COUNT = 100;

    // 저장 방식을 바꿀 수 있도록 PostRepository 인터페이스 사용
    private final PostRepository repository;

    // 마지막으로 발급한 게시글 ID, JPA 사용시 지울 수 있음
    private long sequence;

    // 저장소를 직접 생성하지 않고 외부에서 전달받음. 즉, PostService는 PostRepository 구현체에 직접 의존하지 않음
    // Spring에서도 같은 생성자 사용,  Bean으로 등록한 저장소 구현체 주입
    public PostService(PostRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    // 게시글 작성
    public PostResponse create(PostRequest request) {
        requireRequest(request);
        Category category = Category.from(request.category());

        // 최대 게시글 수 확인
        if (repository.count() >= MAX_POST_COUNT) {
            throw new PostLimitExceededException(MAX_POST_COUNT);
        }

        // 새 ID를 발급하고 입력값을 검증하여 게시글 생성
        Post post = Post.create(++sequence, request.title(), request.content(),
                category, LocalDateTime.now());
        repository.save(post);

        // 저장된 게시글을 전달용 데이터로 변환
        return PostResponse.from(post);
    }

    // 게시글 목록 조회
    public List<PostResponse> findAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparingLong(Post::getId))
                .map(PostResponse::from)
                .toList();
    }

    // 게시글 단건 조회
    public PostResponse findById(long id) {
        return PostResponse.from(getPost(id));
    }

    // 게시글 수정
    public PostResponse update(long id, PostRequest request) {
        requireRequest(request);
        Category category = Category.from(request.category());

        // 수정할 게시글 조회, 새 입력값과 현재 시각으로 게시글 생성
        Post updatedPost = getPost(id).update(request.title(), request.content(), category, LocalDateTime.now());
        repository.save(updatedPost);

        return PostResponse.from(updatedPost);
    }

    // 게시글 삭제
    public void delete(long id) {
        // 삭제된 게시글이 없으면 예외 발생
        if (!repository.deleteById(id)) {
            throw new PostNotFoundException(id);
        }
    }

    // 카테고리 목록 조회
    public List<CategoryResponse> getCategories() {
        // Category에 정의된 값으로 선택 목록 생성
        return Arrays.stream(Category.values())
                .map(category -> new CategoryResponse(category.name(), category.getDisplayName()))
                .toList();
    }

    // 게시글 조회,  존재하지 않으면 예외 발생
    private Post getPost(long id) {
        // 조회 결과가 없으면 orElseThrow로 예외 발생
        return repository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    // 게시글 입력 여부 확인
    private void requireRequest(PostRequest request) {
        if (request == null) {
            throw new InvalidPostException("게시글 입력이 필요합니다.");
        }
    }
}
