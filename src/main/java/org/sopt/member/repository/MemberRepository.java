package org.sopt.member.repository;

import org.sopt.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA에서 회원 저장소 구현체 생성
public interface MemberRepository extends JpaRepository<Member, Long> {
}
