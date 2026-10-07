package org.sopt.post.service.port;

import java.util.List;
import java.util.Optional;

import org.sopt.post.domain.Post;

// Service에서 사용할 저장소 기능 정의
// 같은 인터페이스를 구현하여 HashMap과 List 저장소 교체 가능
// Spring Data JPA와 독립적인 저장소 계약 / DB 도입 시 이 인터페이스를 구현하는 어댑터 추가
public interface PostRepository {

    // 게시글 저장 / 같은 ID가 있으면 수정한 게시글로 교체
    void save(Post post);

    // 게시글 조회 / 조회 결과가 없을 수 있어 Optional 사용
    Optional<Post> findById(long id);

    // 게시글 목록의 복사본 반환 / 정렬은 Service에서 처리
    List<Post> findAll();

    // 게시글 삭제 / 삭제 여부 반환
    boolean deleteById(long id);

    int count();
}
