package org.sopt.post.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import jakarta.transaction.Transactional;
import org.sopt.member.domain.Member;
import org.sopt.member.exception.MemberErrorCode;
import org.sopt.member.exception.MemberException;
import org.sopt.member.repository.MemberRepository;
import org.sopt.post.domain.Category;
import org.sopt.post.domain.Post;
import org.sopt.common.exception.BusinessException;
import org.sopt.common.exception.CommonErrorCode;
import org.sopt.post.exception.PostErrorCode;
import org.sopt.post.exception.PostException;
import org.sopt.post.service.dto.CategoryResponse;
import org.sopt.post.service.dto.PostCreateRequest;
import org.sopt.post.service.dto.PostRequest;
import org.sopt.post.service.dto.PostResponse;
import org.sopt.post.service.dto.PostPageResponse;
import org.sopt.post.repository.PostRepository;
import org.springframework.stereotype.Service;

// 게시글 처리와 JPA 저장소 호출 담당
@Service
public class PostService {

    // 모든 Service 객체에서 같은 작성 한도를 사용하도록 static final 지정
    private static final int MAX_POST_COUNT = 100;
    private static final int MAX_PAGE_SIZE = 100;

    // Spring Data JPA 저장소 주입
    private final PostRepository repository;
    private final MemberRepository memberRepository;

    public PostService(PostRepository repository, MemberRepository memberRepository) {
        this.repository = Objects.requireNonNull(repository);
        this.memberRepository = Objects.requireNonNull(memberRepository);
    }

    // 게시글 작성
    // synchronized의 자세한 원리 더 공부하기...
    @Transactional
    public synchronized PostResponse create(PostCreateRequest request) {
        if (request == null || request.memberId() == null || request.memberId() <= 0) {
            throw new PostException(PostErrorCode.INVALID_POST, "작성자 ID는 양수여야 합니다.");
        }
        // 요청한 회원이 실제로 존재할 때만 작성자로 연결
        Member author = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        Category category = Category.from(request.category());

        // 최대 게시글 수 확인
        if (repository.count() >= MAX_POST_COUNT) {
            throw new PostException(PostErrorCode.POST_LIMIT_EXCEEDED,
                    "게시글은 최대 " + MAX_POST_COUNT + "개까지 작성할 수 있습니다.");
        }

        // 입력값을 검증하여 게시글 생성
        Post post = Post.create(request.title(), request.content(),
                category, author, LocalDateTime.now());
        // 저장소에서 발급한 ID가 포함된 게시글 반환
        Post savedPost = repository.save(post);

        // 저장된 게시글을 전달용 데이터로 변환
        return PostResponse.from(savedPost);
    }

    // 게시글 목록 조회
    // 유효성 검사 controller 에서 할까 고민했지만, service에 엉뚱한 값이 예상 못 한 경로로 들어오는 것을 막기 위해 보수적으로 service에서 검증하기로 결정
    // 지연 로딩된 작성자 이름을 응답으로 변환할 때까지 영속성 컨텍스트 유지
    @Transactional
    public synchronized PostPageResponse findAll(int page, int size, String category) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST,
                    "page는 0 이상, size는 1 이상 " + MAX_PAGE_SIZE + " 이하여야 합니다.");
        }

        Category selectedCategory;
        if (category == null) {
            selectedCategory = null;
        } else {
            selectedCategory = Category.from(category);
        }

        // 카테고리 필터 후 ID 순서로 정렬
        List<Post> filteredPosts = repository.findAll().stream()
                .filter(post -> selectedCategory == null || post.getCategory() == selectedCategory)
                .sorted(Comparator.comparingLong(Post::getId))
                .toList();

        // 큰 page 값의 곱셈 오류를 막기 위해 long으로 시작 위치 계산
        long offset = (long) page * size;
        List<PostResponse> items = filteredPosts.stream()
                .skip(offset)
                .limit(size)
                .map(PostResponse::from)
                .toList();

        // 필터링된 전체 게시글 수를 기준으로 페이지 수 계산
        int totalElements = filteredPosts.size();
        int totalPages = (totalElements + size - 1) / size;
        return new PostPageResponse(items, page, size, totalElements, totalPages);
    }

    // 게시글 단건 조회
    @Transactional
    public synchronized PostResponse findById(long id) {
        Post post = getPost(id);

        // 상세 조회에서만 조회수 증가 후 저장
        Post viewedPost = post.increaseViewCount();
        Post savedPost = repository.save(viewedPost);
        return PostResponse.from(savedPost);
    }

    // 게시글 수정
    @Transactional
    public  PostResponse update(long id, PostRequest request) {
        requireRequest(request);
        Category category = Category.from(request.category());

        // 수정할 게시글 조회, 새 입력값과 현재 시각으로 게시글 생성
        Post updatedPost = getPost(id).update(request.title(), request.content(), category, LocalDateTime.now());
        Post savedPost = repository.save(updatedPost);

        return PostResponse.from(savedPost);
    }

    // 게시글 삭제
    public synchronized void delete(long id) {
        // 존재 여부 확인 후 삭제 / 없는 게시글은 공통 조회 메서드에서 예외 발생
        Post post = getPost(id);
        repository.delete(post);
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
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
    }

    // 게시글 입력 여부 확인
    private void requireRequest(PostRequest request) {
        if (request == null) {
            throw new PostException(PostErrorCode.INVALID_POST, "게시글 입력이 필요합니다.");
        }
    }
}
