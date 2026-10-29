package org.sopt.post.repository;

import org.sopt.post.domain.Category;
import org.sopt.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Spring Data JPA 기본 기능 외에 직접 구현할 게시글 검색
public interface    PostSearchRepository {
    Page<Post> search(Long memberId, Category category, String title, Pageable pageable);
}
