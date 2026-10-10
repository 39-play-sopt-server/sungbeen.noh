package org.sopt;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.sopt.client.PostView;
import org.sopt.post.controller.response.ConsoleResponse;
import org.sopt.post.controller.PostController;
import org.sopt.post.repository.HashMapPostRepository;
import org.sopt.post.service.PostService;
import org.sopt.post.service.dto.CategoryResponse;
import org.sopt.post.service.dto.PostRequest;
import org.sopt.post.service.dto.PostResponse;
import org.sopt.post.service.port.PostRepository;

// View에서 입력받은 값을 Controller에 전달하고 응답을 화면으로 연결
// 현재는 하나의 프로그램 안에서 메서드 호출로 클라이언트와 서버 역할 구분
public final class Main {

    private final PostView view;
    private final PostController controller;

    public Main(PostView view, PostController controller) {
        this.view = Objects.requireNonNull(view);
        this.controller = Objects.requireNonNull(controller);
    }

    // 현재는 직접 객체 생성과 의존성 주입
    // Spring 전환 시 Bean으로 등록한 서버 객체의 생성과 연결은 컨테이너 담당
    public static void main(String[] args) {
        // 게시글 저장소 생성, List 저장소 사용 시 new ListPostRepository()로 변경
        PostRepository repository = new HashMapPostRepository();

        // Service에 저장소 전달, Controller에 Service 연결
        PostService service = new PostService(repository);
        PostController controller = new PostController(service);

        // 콘솔 화면 생성 후 프로그램 실행
        PostView view = new PostView();
        new Main(view, controller).run();
    }

    // 콘솔 메뉴 반복, REST 전환 시 클라이언트에서 사용자 조작에 따라 요청 전송
    public void run() {
        try {
            // 메뉴에서 사용할 카테고리 목록 조회
            ConsoleResponse<List<CategoryResponse>> categories = controller.getCategories();
            // 실패시 안내 후 프로그램 종료
            if (!categories.success()) {
                view.showResponse(categories);
                return;
            }

            boolean running = true;
            while (running) {
                running = handleCommand(view.inputMainMenu(), categories.data());
            }
        } catch (NoSuchElementException exception) {
            // 입력이 끝나면 프로그램 종료
            view.showExitMessage();
        } catch (UncheckedIOException exception) {
            // 입력을 읽을 수 없으면 안내 후 종료
            view.showInputError();
        }
    }

    // 메뉴 명령 처리
    private boolean handleCommand(int command, List<CategoryResponse> categories) {
        switch (command) {
            // 메뉴 명령  별로 입력, 요청, 출력 메서드를 분리
            // 1. 게시글 생성
            case 1 -> createPost(categories);

            // 2. 게시글 목록 조회
            case 2 -> showPostList();

            // 3. 게시글 상세 조회
            case 3 -> showPostResponse(controller.findById(view.inputPostId("조회")));

            // 4. 게시글 수정
            case 4 -> updatePost(categories);

            // 5. 게시글 삭제
            case 5 -> view.showResponse(controller.delete(view.inputPostId("삭제")));

            // 6. 종료
            case 6 -> {
                view.showExitMessage();
                return false;
            }
            default -> view.showInvalidCommandMessage();
        }
        return true;
    }

    // 클라이언트의 동작을 담당하는 메서드, REST에서는 클라이언트에서 컨트롤러에게 전달
    private void createPost(List<CategoryResponse> categories) {
        // View에서 제목, 내용, 카테고리를 입력받아 요청 데이터로 반환
        PostRequest request = view.inputPost("게시글 작성", categories);
        // 입력값을 Controller에 전달하고 작성 결과 수신
        // REST 전환 시 메서드 호출 대신 HTTP 요청으로 전달
        ConsoleResponse<PostResponse> response = controller.create(request);
        // 전달받은 응답을 화면에 표시
        showPostResponse(response);
    }

    // 게시글 목록의 요청과 출력 진행
    private void showPostList() {
        // Controller에 목록 조회 요청 후 결과 수신
        ConsoleResponse<List<PostResponse>> response = controller.findAll();
        // View에서 처리 결과 안내
        view.showResponse(response);
        // 조회 성공 시 응답에 담긴 게시글 목록 출력
        if (response.success()) {
            view.showPostList(response.data());
        }
    }

    // 게시글 수정의 입력, 요청, 출력 진행
    private void updatePost(List<CategoryResponse> categories) {
        // View에서 수정할 게시글 ID 입력
        long id = view.inputPostId("수정");

        // 수정 내용을 입력받기 전에 Controller에 게시글 조회 요청
        // 사용자 편의를 위한 사전 확인 / 실제 수정 대상 확인은 Service에서 다시 처리
        ConsoleResponse<PostResponse> existingPost = controller.findById(id);
        // 조회 실패 시 안내 후 수정 입력 중단
        if (!existingPost.success()) {
            view.showResponse(existingPost);
            return;
        }

        // View에서 수정할 제목, 내용, 카테고리 입력
        PostRequest request = view.inputPost("게시글 수정", categories);
        // 게시글 ID와 입력값을 Controller에 전달하고 수정 결과 수신
        ConsoleResponse<PostResponse> response = controller.update(id, request);
        // 전달받은 응답을 화면에 표시
        showPostResponse(response);
    }

    // 서버에서 받은 응답을 View에 전달하여 출력
    private void showPostResponse(ConsoleResponse<PostResponse> response) {
        // 성공 여부와 관계없이 응답 메시지 출력
        // REST 전환 시 클라이언트에서 JSON 응답을 받아 화면 표시
        view.showResponse(response);
        // 성공한 경우 응답에 담긴 게시글 상세 출력
        if (response.success()) {
            view.showPostDetail(response.data());
        }
    }
}
