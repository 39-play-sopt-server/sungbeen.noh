package org.sopt.post.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.sopt.post.service.port.PostRepository;
import org.sopt.post.domain.Post;

// HashMap을 사용하여 게시글을 저장하는 PostRepository 구현체
// PostRepository에서 사용하겠다고 정의한 메서드를 구현
public final class HashMapPostRepository implements PostRepository {

    // final을 사용하여 저장소를 변경할 수 없도록 지정
    private final Map<Long, Post> posts = new HashMap<>();

    // PostRepository에 정의된 메서드를 구현하도록 @Override 지정
    @Override
    public void save(Post post) {
        Objects.requireNonNull(post);
        posts.put(post.getId(), post);
    }

    @Override
    public Optional<Post> findById(long id) {
        // 게시글 ID를 key로 사용하여 조회
        return Optional.ofNullable(posts.get(id));
    }

    @Override
    public List<Post> findAll() {
        // 호출한 곳에서 저장소를 직접 변경하지 못하도록 복사본 반환
        return List.copyOf(posts.values());
    }

    @Override
    public boolean deleteById(long id) {
        return posts.remove(id) != null;
    }

    @Override
    public int count() {
        return posts.size();
    }
}
