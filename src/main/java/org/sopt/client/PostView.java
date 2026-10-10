package org.sopt.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.sopt.post.controller.response.ConsoleResponse;
import org.sopt.post.service.dto.CategoryResponse;
import org.sopt.post.service.dto.PostRequest;
import org.sopt.post.service.dto.PostResponse;

// 사용자 입력과 게시글 출력
// REST 전환 시 웹이나 앱 화면이 담당할 콘솔 입출력
public final class PostView {

    private final BufferedReader reader;
    private final PrintStream output;

    // 콘솔에서 사용할 입출력 객체 생성
    public PostView() {
        // 성능 향상을 위해 조금이라도 빠른 BufferedReader를 사용
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.output = System.out;
    }

    // 메인 메뉴 입력
    public int inputMainMenu() {
        output.println("\n=== 게시판 ===");
        output.println("1. 게시글 작성");
        output.println("2. 게시글 목록 조회");
        output.println("3. 게시글 단건 조회");
        output.println("4. 게시글 수정");
        output.println("5. 게시글 삭제");
        output.println("6. 종료");
        return inputNumber("선택: ");
    }

    // 게시글 입력, 출력을 매개변수로 입력 받아 재사용 가능하도록 구현
    public PostRequest inputPost(String heading, List<CategoryResponse> categories) {
        output.println("\n=== " + heading + " ===");

        // 게시글 제목과 내용 입력
        output.print("제목: ");
        String title = readLine();
        output.print("본문: ");
        String content = readLine();

        // 선택 가능한 카테고리 출력
        for (int index = 0; index < categories.size(); index++) {
            CategoryResponse category = categories.get(index);
            output.println((index + 1) + ". " + category.displayName() + " (" + category.code() + ")");
        }

        while (true) {
            int selection = inputNumber("카테고리 선택: ");
            if (selection >= 1 && selection <= categories.size()) {
                return new PostRequest(title, content, categories.get(selection - 1).code());
            }
            output.println("목록에 있는 카테고리 번호를 입력해주세요.");
        }
    }

    // 게시글 ID 입력
    public long inputPostId(String action) {
        while (true) {
            output.print(action + "할 게시글 ID: ");
            try {
                // 입력받은 문자열을 게시글 ID에 사용하는 long 타입으로 변환, BufferedReader는 primitive 타입으로 바로 받을 수 없음
                return Long.parseLong(readLine().strip());  // strip()으로 공백 제거
            } catch (NumberFormatException exception) {
                output.println("유효한 정수 ID를 입력해주세요.");
            }
        }
    }

    // 숫자 입력
    private int inputNumber(String message) {
        while (true) {
            output.print(message);
            try {
                // 마찬가지로 primitive 타입으로 변환
                return Integer.parseInt(readLine().strip());
            } catch (NumberFormatException exception) {
                output.println("숫자를 입력해주세요.");
            }
        }
    }

    // 문자열 입력, BufferedReader.readLine()은 IOException을 발생시킬 수 있으므로 예외 처리
    private String readLine() {
        try {
            String input = reader.readLine();
            if (input == null) {
                // 입력이 끝나면 Main에 종료 전달
                // 콘솔 입력 종료 처리, REST 서버의 종료와는 별개
                throw new NoSuchElementException("입력이 종료되었습니다.");
            }
            return input;
        } catch (IOException exception) {
            throw new UncheckedIOException("입력을 읽는 중 오류가 발생했습니다.", exception);
        }
    }

    // 처리 결과 출력
    public void showResponse(ConsoleResponse<?> response) {
        // 게시글이나 목록 등 데이터 종류에 관계없이 메시지 출력
        output.println(response.message());
    }

    // 게시글 목록 출력
    public void showPostList(List<PostResponse> posts) {
        output.println("\n=== 게시글 목록 ===");
        if (posts.isEmpty()) {
            output.println("게시글이 없습니다.");
            return;
        }

        for (PostResponse post : posts) {
            // 목록 순서 대신 게시글에 부여된 ID 출력
            output.println("ID " + post.id() + " | [" + post.categoryName() + "] " + post.title());
        }
    }

    // 게시글 상세 출력
    public void showPostDetail(PostResponse post) {
        output.println("\n=== 게시글 상세 ===");
        output.println("ID: " + post.id());
        output.println("제목: " + post.title());
        output.println("본문: " + post.content());
        output.println("카테고리: " + post.categoryName());
        // 별도 형식 변환 없이 저장된 날짜와 시각 출력
        output.println("작성 시각: " + post.createdAt());
        output.println("수정 시각: " + post.updatedAt());
    }

    // 잘못된 메뉴 입력 안내
    public void showInvalidCommandMessage() {
        output.println("메뉴에 있는 번호를 입력해주세요.");
    }

    // 입력 오류 안내
    public void showInputError() {
        output.println("입력을 읽을 수 없어 프로그램을 종료합니다.");
    }

    // 프로그램 종료 안내
    public void showExitMessage() {
        output.println("프로그램을 종료합니다.");
    }
}
