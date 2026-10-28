package org.sopt.post.repository;

import org.sopt.post.domain.Category;
import org.sopt.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Spring Data JPA에서 DB 접근 구현체 생성 / 기본 CRUD 메서드 제공
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 회원 삭제 전 작성한 게시글 존재 여부 확인
    boolean existsByAuthorId(Long memberId);

    // 작성자는 지연 로딩되어 이름에 접근할 때 추가 조회가 발생할 수 있음
    // 목록에서 게시글마다 회원을 조회하는 상황을 줄이도록 작성자도 함께 조회
    @Override
    @EntityGraph(attributePaths = "author")
    Page<Post> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Post> findByCategory(Category category, Pageable pageable);

}
