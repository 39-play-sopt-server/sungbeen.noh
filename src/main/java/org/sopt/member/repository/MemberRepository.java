package org.sopt.member.repository;

import org.sopt.member.domain.Member;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA에서 회원 저장소 구현체 생성
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 회원 삭제와 게시글·좋아요 생성 시 같은 회원 행을 잠금
    // 트랜잭션이 끝날 때까지 다른 요청이 해당 행의 쓰기 잠금을 기다림
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findByIdForUpdate(@Param("id") Long id);
}
