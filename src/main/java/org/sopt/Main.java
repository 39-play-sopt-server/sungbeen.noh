package org.sopt;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.sopt.post.PostController;
import org.sopt.post.PostView;

public class Main {

    public static void main(String[] args) {
        // 프로그램에서 사용할 콘솔 입력 객체 생성
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(System.in)
        );

        // View가 필요한 BufferedReader를 외부에서 전달
        PostView postView = new PostView(bufferedReader);

        // Controller가 필요한 View를 외부에서 전달
        PostController postController = new PostController(postView);

        // 프로그램 실행
        postController.run();
    }
}