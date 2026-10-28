package org.sopt.common.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// 여러 엔티티에서 공통으로 사용하는 생성일 관리
// @MappedSuperclass: 별도 테이블 없이 상속받은 엔티티의 테이블에 필드 추가
// @EntityListeners: 엔티티 저장 시 Auditing이 생성일을 설정하도록 연결
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    // 처음 저장할 때 Auditing이 생성일 설정 / 이후 JPA 수정 쿼리에는 포함하지 않음
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
