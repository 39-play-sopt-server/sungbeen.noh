package org.sopt.member.service.dto;

import org.sopt.member.domain.Member;

public record MemberResponse(Long id, String name) {

    // 회원을 전달용 데이터로 변환
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName());
    }
}
