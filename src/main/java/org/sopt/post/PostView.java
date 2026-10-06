package org.sopt.post;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class PostView {
    private final BufferedReader bufferedReader;

    public PostView(BufferedReader bufferedReader) {
        // 콘솔 입력 객체를 외부에서 전달받아 재사용
        this.bufferedReader = Objects.requireNonNull(bufferedReader);
    }

    // 메인 메뉴 입력
    public int inputMainMenu() {
        System.out.println("\n=== 게시판 ===");
        System.out.println("1. 게시글 작성");
        System.out.println("2. 게시글 목록 조회");
        System.out.println("3. 게시글 단건 조회");
        System.out.println("4. 게시글 수정");
        System.out.println("5. 게시글 삭제");
        System.out.println("6. 종료");

        return inputNumber("선택: ");
    }

    // 게시글 제목 입력
    public String inputPostTitle() {
        System.out.println("\n=== 게시글 작성 ===");
        System.out.print("제목: ");

        return readLine();
    }

    // 게시글 내용 입력
    public String inputPostContent() {
        System.out.print("내용: ");

        return readLine();
    }

    // 게시글 단건 조회 번호 입력
    public int inputPostDetailNumber() {
        return inputPostNumber(
                "=== 게시글 단건 조회 ===",
                "조회할 게시글 번호: "
        );
    }

    // 게시글 수정 번호 입력
    public int inputPostUpdateNumber() {
        return inputPostNumber(
                "=== 게시글 수정 ===",
                "수정할 게시글 번호: "
        );
    }

    // 게시글 삭제 번호 입력
    public int inputPostDeleteNumber() {
        return inputPostNumber(
                "=== 게시글 삭제 ===",
                "삭제할 게시글 번호: "
        );
    }

    // 게시글 번호 입력을 공통으로 처리
    private int inputPostNumber(String title, String message) {
        System.out.println("\n=== " + title + " ===");

        return inputNumber(message);
    }

    // 게시글 수정 제목 입력
    public String inputPostUpdateTitle() {
        System.out.print("수정할 제목: ");

        return readLine();
    }

    // 게시글 수정 내용 입력
    public String inputPostUpdateContent() {
        System.out.print("수정할 내용: ");

        return readLine();
    }

    // 문자열 입력
    private String readLine() {
        try {
            String input = bufferedReader.readLine();

            if (input == null) {
                throw new IllegalStateException("입력 스트림이 종료되었습니다.");
            }

            return input;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "입력을 읽는 중 오류가 발생했습니다.",
                    e
            );
        }
    }

    // 숫자 입력
    private int inputNumber(String message) {
        while (true) {
            System.out.print(message);

            try {
                // BufferedReader는 문자열을 반환하므로 int 타입으로 변환
                return Integer.parseInt(readLine());
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    // 게시글 작성 완료
    public void showPostCreationComplete() {
        System.out.println("게시글이 작성되었습니다.");
    }

    // 최대 게시글 수 초과
    public void showPostCreationLimitExceeded() {
        System.out.println("더 이상 게시글을 작성할 수 없습니다.");
    }

    // 게시글 목록
    public void showPostList(List<Post> posts) {
        System.out.println("\n=== 게시글 목록 ===");

        for (int i = 0; i < posts.size(); i++) {
            Post currentPost = posts.get(i);

            System.out.println(
                    (i + 1) + ". " + currentPost.getTitle()
            );
        }
    }

    // 게시글이 없을 때
    public void showNoPostsMessage() {
        System.out.println("게시글이 없습니다.");
    }

    // 게시글 단건 조회
    public void showPostDetail(Post post) {
        System.out.println("\n=== 게시글 내용 ===");
        System.out.println("제목: " + post.getTitle());
        System.out.println("내용: " + post.getContent());
    }

    // 유효하지 않은 게시글 번호
    public void showInvalidPostNumberMessage() {
        System.out.println("존재하지 않는 게시글입니다.");
    }

    // 게시글 수정 완료
    public void showPostUpdateComplete() {
        System.out.println("게시글이 수정되었습니다.");
    }

    // 게시글 삭제 완료
    public void showPostDeleteComplete() {
        System.out.println("게시글이 삭제되었습니다.");
    }

    // 잘못된 메뉴 입력
    public void showInvalidCommandMessage() {
        System.out.println("잘못된 입력입니다.");
    }

    // 프로그램 종료
    public void showExitMessage() {
        System.out.println("프로그램을 종료합니다.");
    }
}