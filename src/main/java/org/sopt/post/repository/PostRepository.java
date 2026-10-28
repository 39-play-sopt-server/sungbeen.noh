package org.sopt.post.repository;

import org.sopt.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Spring Data JPA에서 DB 접근 구현체 생성 / 기본 CRUD 메서드 제공
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 회원 삭제 전 작성한 게시글 존재 여부 확인
    boolean existsByAuthorId(Long memberId);

    Optional<Post> findById(Long id);

    List<Post> findAll();

    void deleteById(Long id);
}
