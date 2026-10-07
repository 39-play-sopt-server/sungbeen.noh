package org.sopt.post.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.sopt.post.service.port.PostRepository;
import org.sopt.post.domain.Post;

// 저장 방식 비교를 위한 List 기반 게시글 저장소, 실제로 안씀
public final class ListPostRepository implements PostRepository {

    private final List<Post> posts = new ArrayList<>();

    @Override
    public void save(Post post) {
        Objects.requireNonNull(post, "게시글은 null일 수 없습니다.");

        // 같은 ID가 있으면 수정한 게시글로 교체
        for (int index = 0; index < posts.size(); index++) {
            if (posts.get(index).getId() == post.getId()) {
                posts.set(index, post);
                return;
            }
        }

        // 새로운 게시글 추가
        posts.add(post);
    }

    @Override
    public Optional<Post> findById(long id) {
        // 목록을 순회하며 ID가 같은 게시글 조회
        return posts.stream()
                .filter(post -> post.getId() == id)
                .findFirst();
    }

    @Override
    public List<Post> findAll() {
        return List.copyOf(posts);
    }

    @Override
    public boolean deleteById(long id) {
        return posts.removeIf(post -> post.getId() == id);
    }

    @Override
    public int count() {
        return posts.size();
    }
}
