package org.sopt.post.repository;

import org.sopt.post.domain.Post;
import java.util.Optional;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// Spring Data JPA에서 DB 접근 구현체 생성 / 기본 CRUD 메서드 제공
@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostSearchRepository {

    // 회원 삭제 전 작성한 게시글 존재 여부 확인
    boolean existsByAuthorId(Long memberId);

    // 조회수·좋아요 등 같은 게시글을 변경하는 요청은 쓰기 잠금을 사용
    // 트랜잭션이 끝날 때까지 다른 요청의 같은 행 변경을 기다리게 함
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :id")
    Optional<Post> findByIdForUpdate(@Param("id") Long id);

    // 회원이 좋아요를 누른 게시글 ID를 잠금 순서가 일정하도록 오름차순 조회
    @Query("select p.id from Post p join p.likedMembers m where m.id = :memberId order by p.id")
    List<Long> findLikedPostIds(@Param("memberId") Long memberId);

}
