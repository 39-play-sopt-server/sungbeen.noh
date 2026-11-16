package org.sopt.auth;

import java.util.Base64;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.auth.security.JwtProvider;
import org.sopt.member.repository.MemberRepository;
import org.sopt.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 실제 필터·서비스·JPA를 함께 실행하되 테스트 DB만 사용
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.datasource.url=jdbc:h2:mem:auth;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false", "spring.jpa.properties.hibernate.format_sql=false",
        "jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "jwt.access-token-expiration=1800000"
})
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired JsonMapper mapper;
    @Autowired MemberRepository members;
    @Autowired PostRepository posts;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

    @BeforeEach
    void clearDatabase() {
        posts.deleteAll();
        members.deleteAll();
    }

    @Test
    void signupHashesPasswordAndLoginReturnsUsableToken() throws Exception {
        long id = signup("  User@Example.com  ");
        var member = members.findById(id).orElseThrow();
        assertEquals("user@example.com", member.getEmail());
        assertNotEquals("password123", member.getPassword());
        assertTrue(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .matches("password123", member.getPassword()));
        String token = login("USER@example.com");
        mvc.perform(get("/api/v1/members/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void duplicateEmailIsConflictEvenWithDifferentCase() throws Exception {
        signup("user@example.com");
        mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("USER@example.com", "password123")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
        assertEquals(1, members.count());
    }

    @Test
    void invalidSignupInputIsRejectedWithoutSaving() throws Exception {
        for (String email : new String[]{"", "bad", "a..b@example.com", "a@-example.com", "a@exam ple.com",
                "a".repeat(65) + "@example.com"}) {
            mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                            .content(signupBody(email, "password123")))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_MEMBER"));
        }
        for (String password : new String[]{"", "short", "a".repeat(73), "가".repeat(25)}) {
            mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                            .content(signupBody("user@example.com", password)))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("name", " ", "email", "user@example.com",
                                "password", "password123"))))
                .andExpect(status().isBadRequest());
        assertEquals(0, members.count());
    }

    @Test
    void bcryptAcceptsExactly72Bytes() throws Exception {
        String password = "가".repeat(24);
        mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("user@example.com", password)))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", "user@example.com", "password", password))))
                .andExpect(status().isOk());
    }

    @Test
    void legacyMemberIsPreservedAndCannotLoginWithoutCredentials() throws Exception {
        jdbc.update("insert into members (name, created_at) values (?, CURRENT_TIMESTAMP)", "기존 회원");
        assertEquals(1, members.count());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", "legacy@example.com", "password", "password123"))))
                .andExpect(status().isUnauthorized());
        signup("new@example.com");
        assertEquals(2, members.count());
    }

    @Test
    void missingAndMalformedBodiesAreBadRequests() throws Exception {
        for (String body : new String[]{"", "null", "{", "{}"}) {
            mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
            mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void wrongPasswordAndUnknownEmailHaveSameResponse() throws Exception {
        signup("user@example.com");
        String previous = null;
        for (String email : new String[]{"user@example.com", "unknown@example.com"}) {
            String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(Map.of("email", email, "password", "incorrect123"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                    .andReturn().getResponse().getContentAsString();
            if (previous != null) assertEquals(previous, body);
            previous = body;
        }
    }

    @Test
    void missingMalformedForgedExpiredAndInvalidSubjectTokensAreUnauthorized() throws Exception {
        long id = signup("user@example.com");
        var key = Keys.hmacShaKeyFor(Base64.getDecoder().decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="));
        String expired = Jwts.builder().subject(Long.toString(id))
                .expiration(new Date(System.currentTimeMillis() - 60000)).signWith(key).compact();
        String forged = new JwtProvider(Base64.getEncoder().encodeToString(new byte[64]), 1800000)
                .createAccessToken(id);
        String invalidSubject = Jwts.builder().subject("not-a-number")
                .expiration(new Date(System.currentTimeMillis() + 60000)).signWith(key).compact();
        String noExpiration = Jwts.builder().subject(Long.toString(id)).signWith(key).compact();
        for (String token : new String[]{"", "broken", expired, forged, invalidSubject, noExpiration}) {
            mvc.perform(get("/api/v1/members/me").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }
        mvc.perform(get("/api/v1/posts")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenCannotBeReusedAfterWithdrawal() throws Exception {
        long id = signup("user@example.com");
        String token = login("user@example.com");
        mvc.perform(delete("/api/v1/members/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/members/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeterminesPostAuthorAndLikeMemberAndOnlyAuthorCanModify() throws Exception {
        long ownerId = signup("owner@example.com");
        long otherId = signup("other@example.com");
        String owner = login("owner@example.com");
        String other = login("other@example.com");
        String postJson = mapper.writeValueAsString(Map.of("title", "제목", "content", "본문",
                "category", "FREE", "memberId", otherId));
        String created = mvc.perform(post("/api/v1/posts").header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON).content(postJson))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long postId = mapper.readTree(created).path("data").path("id").asLong();
        assertEquals(ownerId, posts.findById(postId).orElseThrow().getAuthor().getId());
        mvc.perform(put("/api/v1/posts/" + postId).header("Authorization", "Bearer " + other)
                        .contentType(MediaType.APPLICATION_JSON).content(postJson))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mvc.perform(delete("/api/v1/posts/" + postId).header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/members/" + ownerId).header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/members/" + ownerId).header("Authorization", "Bearer " + owner))
                .andExpect(status().isConflict());
        // 쿼리의 타인 ID는 무시하고 토큰의 회원으로 좋아요 처리
        mvc.perform(post("/api/v1/posts/" + postId + "/likes").param("memberId", "" + ownerId)
                        .header("Authorization", "Bearer " + other)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/posts/" + postId + "/likes").header("Authorization", "Bearer " + other))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/v1/posts/" + postId + "/likes").header("Authorization", "Bearer " + other))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v1/posts/" + postId).header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON).content(postJson)).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/posts/" + postId).header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk());
    }

    @Test
    void existingMemberCreationPathUsesSameSignupRules() throws Exception {
        mvc.perform(post("/api/v1/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("user@example.com", "password123")))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/members").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"이름\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void corsPreflightAndPublicHealthWorkWithoutToken() throws Exception {
        mvc.perform(options("/api/v1/posts").header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
        mvc.perform(get("/api/v1/health")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    private long signup(String email) throws Exception {
        String response = mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody(email, "password123")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).path("data").path("id").asLong();
    }

    private String signupBody(String email, String password) {
        return mapper.writeValueAsString(Map.of("name", "회원", "email", email, "password", password));
    }

    private String login(String email) throws Exception {
        String response = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", email, "password", "password123"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).path("data").path("accessToken").asString();
    }
}
