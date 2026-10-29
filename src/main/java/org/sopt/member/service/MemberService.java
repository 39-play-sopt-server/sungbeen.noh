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

    // 작성한 게시글이 없다면 좋아요를 정리한 뒤 회원 삭제
    @Transactional
    public void delete(long id) {
        Member member = memberRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 실습에서는 게시글의 작성자가 항상 존재하도록 작성한 게시글이 있으면 삭제 거부
        if (postRepository.existsByAuthorId(id)) {
            throw new MemberException(MemberErrorCode.MEMBER_HAS_POSTS);
        }

        // 회원이 누른 좋아요를 정리 / 게시글 ID 순서로 잠가 잠금 순서가 뒤집히는 상황을 줄임
        for (Long postId : postRepository.findLikedPostIds(id)) {
            // 동시에 삭제된 게시글은 좋아요 관계도 삭제되므로 건너뜀
            postRepository.findByIdForUpdate(postId).ifPresent(post -> post.removeLike(member));
        }

        // 회원 삭제 전에 좋아요 관계 삭제를 DB에 반영해 외래 키 충돌 방지
        postRepository.flush();
        memberRepository.delete(member);
    }
}
