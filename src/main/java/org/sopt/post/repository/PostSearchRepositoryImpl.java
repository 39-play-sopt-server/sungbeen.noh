package org.sopt.post.repository;

import static org.sopt.post.domain.QPost.post;

import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.sopt.post.domain.Category;
import org.sopt.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

// 검색 조건과 페이지를 DB에 전달하는 Querydsl 구현체
public class PostSearchRepositoryImpl implements PostSearchRepository {

    private final JPAQueryFactory queryFactory;

    public PostSearchRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Post> search(Long memberId, Category category, String title, Pageable pageable) {
        // 작성자 이름을 게시글마다 따로 조회하지 않도록 함께 가져옴
        List<Post> posts = queryFactory.selectFrom(post)
                .join(post.author).fetchJoin()
                .where(authorIdEquals(memberId), categoryEquals(category), titleContains(title))
                .orderBy(post.createdAt.desc(), post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이지 수 계산에는 페이지 제한 없이 같은 조건의 전체 게시글 수가 필요
        Long total = queryFactory.select(post.count())
                .from(post)
                .where(authorIdEquals(memberId), categoryEquals(category), titleContains(title))
                .fetchOne();

        return new PageImpl<>(posts, pageable, total == null ? 0 : total);
    }

    // where에 null을 전달하면 해당 조건은 적용하지 않음
    private BooleanExpression authorIdEquals(Long memberId) {
        return memberId == null ? null : post.author.id.eq(memberId);
    }

    private BooleanExpression categoryEquals(Category category) {
        return category == null ? null : post.category.eq(category);
    }

    private BooleanExpression titleContains(String title) {
        return title == null ? null : post.title.contains(title);
    }
}
