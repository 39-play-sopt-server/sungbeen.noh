package org.sopt.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PostController {
    private static final int MAX_POST_COUNT = 100;

    // 게시글 개수는 List가 직접 관리하도록 별도의 postCount를 두지 않는다.
    private final List<Post> posts;

    private final PostView postView;

    public PostController(PostView postView) {
        this.posts = new ArrayList<>(MAX_POST_COUNT);

        // Controller가 View를 직접 생성하지 않고 외부에서 전달받는다.
        this.postView = Objects.requireNonNull(postView);
    }

    // 프로그램 실행
    public void run() {
        boolean running = true;

        while (running) {
            int command = postView.inputMainMenu();

            running = handleCommand(command);
        }
    }

    // 메뉴 명령 처리
    private boolean handleCommand(int command) {
        switch (command) {
            // 1. 게시글 작성
            case 1 -> createPost();

            // 2. 게시글 목록 조회
            case 2 -> showPostList();

            // 3. 게시글 단건 조회
            case 3 -> showPostDetail();

            // 4. 게시글 수정
            case 4 -> updatePost();

            // 5. 게시글 삭제
            case 5 -> deletePost();

            // 6. 종료
            case 6 -> {
                postView.showExitMessage();
                return false;
            }

            default -> postView.showInvalidCommandMessage();
        }

        return true;
    }

    // 게시글 작성
    private void createPost() {
        if (isPostStorageFull()) {
            postView.showPostCreationLimitExceeded();
            return;
        }

        // 게시글 제목 입력
        String title = postView.inputPostTitle();

        // 게시글 내용 입력
        String content = postView.inputPostContent();

        // 게시글 생성 / 저장
        posts.add(Post.create(title, content));

        // 게시글 작성 완료
        postView.showPostCreationComplete();
    }

    // 게시글 목록 조회
    private void showPostList() {
        if (posts.isEmpty()) {
            postView.showNoPostsMessage();
            return;
        }

        // View가 실제 게시글 목록을 직접 변경하지 못하도록 복사본을 전달
        postView.showPostList(List.copyOf(posts));
    }

    // 게시글 단건 조회
    private void showPostDetail() {
        // 조회할 게시글 번호 입력
        int postNumber = postView.inputPostDetailNumber();

        // 입력한 번호로 게시글 선택
        Optional<SelectedPost> selectedPost = selectPost(postNumber);

        if (selectedPost.isEmpty()) {
            return;
        }

        // 선택된 게시글 출력
        postView.showPostDetail(
                selectedPost.get().post()
        );
    }

    // 게시글 수정
    private void updatePost() {
        // 수정할 게시글 번호 입력
        int postNumber = postView.inputPostUpdateNumber();

        // 입력한 번호로 게시글 선택
        Optional<SelectedPost> selectedPost = selectPost(postNumber);

        if (selectedPost.isEmpty()) {
            return;
        }

        // 수정할 게시글 조회
        Post post = selectedPost.get().post();

        // 게시글 제목 입력
        String newTitle = postView.inputPostUpdateTitle();

        // 게시글 내용 입력
        String newContent = postView.inputPostUpdateContent();

        // 게시글 자신의 상태는 Post가 직접 수정
        post.update(newTitle, newContent);

        // 게시글 수정 완료
        postView.showPostUpdateComplete();
    }

    // 게시글 삭제
    private void deletePost() {
        // 삭제할 게시글 번호 입력
        int postNumber = postView.inputPostDeleteNumber();

        // 입력한 번호로 게시글 선택
        Optional<SelectedPost> selectedPost = selectPost(postNumber);

        if (selectedPost.isEmpty()) {
            return;
        }

        // 선택된 게시글 삭제
        posts.remove(selectedPost.get().index());

        // 게시글 삭제 완료
        postView.showPostDeleteComplete();
    }

    // 조회, 수정, 삭제에서 공통으로 사용하는 게시글 선택 처리
    private Optional<SelectedPost> selectPost(int postNumber) {
        if (posts.isEmpty()) {
            postView.showNoPostsMessage();
            return Optional.empty();
        }

        // 사용자에게 보여주는 게시글 번호를 List의 index로 변환
        int postIndex = convertPostNumberToIndex(postNumber);

        if (!isValidPostIndex(postIndex)) {
            postView.showInvalidPostNumberMessage();

            // 유효한 게시글이 없음을 Optional.empty()로 표현
            return Optional.empty();
        }

        // 게시글의 index와 실제 Post 객체를 함께 반환
        return Optional.of(
                new SelectedPost(
                        postIndex,
                        posts.get(postIndex)
                )
        );
    }

    // 최대 게시글 수 확인
    private boolean isPostStorageFull() {
        return posts.size() >= MAX_POST_COUNT;
    }

    // 게시글 번호 유효성 검사
    private boolean isValidPostIndex(int postIndex) {
        return postIndex >= 0 && postIndex < posts.size();
    }

    // 게시글 번호를 List의 index로 변환
    private int convertPostNumberToIndex(int postNumber) {
        return postNumber - 1;
    }

    // 선택된 게시글의 index와 Post 객체를 하나의 값으로 관리
    private record SelectedPost(
            int index,
            Post post
    ) {
    }
}