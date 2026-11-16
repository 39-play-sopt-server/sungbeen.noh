package org.sopt.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.sopt.common.domain.BaseEntity;
import org.sopt.member.exception.MemberErrorCode;
import org.sopt.member.exception.MemberException;

@Entity
@Table(name = "members", uniqueConstraints = @UniqueConstraint(name = "uk_members_email", columnNames = "email"))
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기존 이름 전용 회원을 보존하기 위해 DB에서는 null 허용 / 신규 가입은 생성자에서 필수 검증
    @Column(length = 254)
    private String email;

    // 기존 회원의 비밀번호를 임의로 만들지 않음 / 신규 가입은 BCrypt 해시 저장
    @Column(length = 60)
    private String password;

    @Column(nullable = false)
    private String name;

    // 중요! JPA가 DB에서 조회한 데이터를 엔티티 객체로 복원할 때 사용하는 기본 생성자
    // 직접 다른 생성자를 만들면 자바가 기본 생성자를 자동으로 만들어주지 않아 별도로 작성
    protected Member() {
    }

    private Member(String name, String email, String encodedPassword) {
        // 서비스 외의 경로에서 생성하더라도 같은 이름 검증 적용
        if (name == null || name.isBlank()) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER, "회원 이름은 필수입니다.");
        }
        // DB 컬럼에 저장할 수 있는 길이 확인
        if (name.length() > 255) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER, "회원 이름은 255자 이하여야 합니다.");
        }
        this.name = name;
        this.email = MemberCredentials.normalizeEmail(email);
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER, "암호화된 비밀번호가 필요합니다.");
        }
        this.password = encodedPassword;
    }

    // 회원 생성
    public static Member create(String name, String email, String encodedPassword) {
        return new Member(name, email, encodedPassword);
    }

    // 회원 ID 조회
    public Long getId() {
        return id;
    }

    // 로그인할 때 저장된 해시와 입력 비밀번호 비교에 사용
    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    // 회원 이름 조회
    public String getName() {
        return name;
    }
}
