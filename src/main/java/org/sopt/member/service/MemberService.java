package org.sopt.member.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import org.sopt.member.domain.Member;
import org.sopt.member.exception.MemberErrorCode;
import org.sopt.member.exception.MemberException;
import org.sopt.member.repository.MemberRepository;
import org.sopt.member.service.dto.MemberCreateRequest;
import org.sopt.member.service.dto.MemberResponse;
import org.sopt.post.repository.PostRepository;
import org.springframework.stereotype.Service;

// 회원 생성과 삭제 처리
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public MemberService(MemberRepository memberRepository, PostRepository postRepository) {
        this.memberRepository = Objects.requireNonNull(memberRepository);
        this.postRepository = Objects.requireNonNull(postRepository);
    }

    // 회원 생성
    @Transactional
    public MemberResponse create(MemberCreateRequest request) {
        if (request == null) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER);
        }

        Member member = Member.create(request.name());
        Member savedMember = memberRepository.save(member);
        return MemberResponse.from(savedMember);
    }

    // 작성한 게시글이 없는 회원만 삭제
    @Transactional
    public void delete(long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 작성자가 없는 게시글이 남지 않도록 삭제 전 확인
        if (postRepository.existsByAuthorId(id)) {
            throw new MemberException(MemberErrorCode.MEMBER_HAS_POSTS);
        }

        memberRepository.delete(member);
    }
}
