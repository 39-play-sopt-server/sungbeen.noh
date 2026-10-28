package org.sopt.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.sopt.member.exception.MemberErrorCode;
import org.sopt.member.exception.MemberException;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // 중요! JPA에서 객체를 조회할 때, 빈 객체를 리플렉션으로 먼저 생성하고 조회를 한 뒤 채워넣는데, 그 때 사용하기 위한 빈 생성자를 만들어줘야 작동한다.
    protected Member() {
    }

    private Member(String name) {
        // 서비스 외의 경로에서 생성하더라도 같은 이름 검증 적용
        if (name == null || name.isBlank()) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER, "회원 이름은 필수입니다.");
        }
        // DB 컬럼에 저장할 수 있는 길이 확인
        if (name.length() > 255) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER, "회원 이름은 255자 이하여야 합니다.");
        }
        this.name = name;
    }

    // 회원 생성
    public static Member create(String name) {
        return new Member(name);
    }

    // 회원 ID 조회
    public Long getId() {
        return id;
    }

    // 회원 이름 조회
    public String getName() {
        return name;
    }
}
