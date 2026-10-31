package org.sopt.post.service;

import java.time.LocalDateTime;
import java.util.Arrays;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @Transactional
    public PostResponse create(PostCreateRequest request) {
        if (request == null || request.memberId() == null || request.memberId() <= 0) {
            throw new PostException(PostErrorCode.INVALID_POST, "작성자 ID는 양수여야 합니다.");
        }
        // 회원을 잠금 조회해 삭제와 게시글 생성이 동시에 진행되는 상황 방지
        Member author = memberRepository.findByIdForUpdate(request.memberId())
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
    public PostPageResponse findAll(int page, int size, String category, Long memberId, String title) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST,
                    "page는 0 이상, size는 1 이상 " + MAX_PAGE_SIZE + " 이하여야 합니다.");
        }

        // 생성일이 같을 때도 순서가 일정하도록 ID를 두 번째 정렬 기준으로 사용
        Sort sort = Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        );
        // 페이지 번호와 크기, 정렬 기준을 DB 조회에 전달
        Pageable pageable = PageRequest.of(page, size, sort);

        if (memberId != null && memberId <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "작성자 ID는 양수여야 합니다.");
        }
        if (title != null && title.isBlank()) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "검색할 제목을 입력해주세요.");
        }

        // 입력하지 않은 조건은 제외하고 DB에서 검색
        Category selectedCategory = category == null ? null : Category.from(category);
        Page<Post> posts = repository.search(memberId, selectedCategory, title, pageable);
        return PostPageResponse.from(posts);
    }

    // 게시글 단건 조회
    @Transactional
    public PostResponse findById(long id) {
        Post post = getPostForUpdate(id);

        // 상세 조회에서만 조회수 증가
        // 트랜잭션 안에서 관리 중인 엔티티를 변경하면 JPA가 변경 내용을 반영
        post.increaseViewCount();
        return PostResponse.from(post);
    }

    // 게시글 수정
    @Transactional
    public  PostResponse update(long id, PostRequest request) {
        requireRequest(request);
        Category category = Category.from(request.category());

        // 조회한 게시글의 내용과 수정 시각 변경
        Post post = getPostForUpdate(id);
        post.update(request.title(), request.content(), category, LocalDateTime.now());

        return PostResponse.from(post);
    }

    // 게시글 삭제
    @Transactional
    public void delete(long id) {
        // 게시글을 잠금 조회한 뒤 삭제 / 존재하지 않으면 예외 발생
        Post post = getPostForUpdate(id);
        repository.delete(post);
    }

    // 좋아요 추가·취소는 회원 → 게시글 순서로 잠금
    // 같은 순서를 사용해 잠금 순서가 뒤집혀 서로 기다리는 상황을 줄임
    @Transactional
    public void addLike(long postId, long memberId) {
        Member member = getMemberForLike(memberId);
        Post post = getPostForUpdate(postId);
        post.addLike(member);
    }

    @Transactional
    public void removeLike(long postId, long memberId) {
        Member member = getMemberForLike(memberId);
        Post post = getPostForUpdate(postId);
        post.removeLike(member);
    }

    private Member getMemberForLike(long memberId) {
        if (memberId <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST, "회원 ID는 양수여야 합니다.");
        }
        return memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private Post getPostForUpdate(long id) {
        return repository.findByIdForUpdate(id)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
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
