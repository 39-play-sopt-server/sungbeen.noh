package org.sopt.auth;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.sopt.member.domain.Member;
import org.sopt.member.exception.MemberException;
import org.sopt.member.exception.MemberErrorCode;
import org.sopt.member.repository.MemberRepository;
import org.sopt.member.service.MemberService;
import org.sopt.member.service.dto.MemberCreateRequest;
import org.sopt.post.repository.PostRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// 사전 중복 확인 뒤 다른 가입 요청이 먼저 저장되는 경우의 DB 오류 변환 확인
class SignupConflictTest {

    @Test
    void concurrentEmailConflictIsReportedAsDuplicate() {
        var members = mock(MemberRepository.class);
        var encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).thenReturn("encoded-password");
        when(members.saveAndFlush(any(Member.class))).thenThrow(new DataIntegrityViolationException(
                "duplicate", new ConstraintViolationException("duplicate", new SQLException(), "uk_members_email")));
        var service = new MemberService(members, mock(PostRepository.class), encoder);

        MemberException exception = assertThrows(MemberException.class, () -> service.create(
                new MemberCreateRequest("회원", "user@example.com", "password123")));
        assertEquals(MemberErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void unrelatedDatabaseFailureIsNotReportedAsDuplicateEmail() {
        var members = mock(MemberRepository.class);
        var encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).thenReturn("encoded-password");
        var failure = new DataIntegrityViolationException("unrelated constraint");
        when(members.saveAndFlush(any(Member.class))).thenThrow(failure);
        var service = new MemberService(members, mock(PostRepository.class), encoder);

        assertSame(failure, assertThrows(DataIntegrityViolationException.class, () -> service.create(
                new MemberCreateRequest("회원", "user@example.com", "password123"))));
    }
}
