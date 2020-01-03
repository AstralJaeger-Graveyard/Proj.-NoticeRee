package org.astraljaeger.noticeree;

import java.util.Arrays;
import javafx.application.Application;
import javafx.stage.Stage;

public class NoticeRee extends Application {

    private static String[] arguments;

    public static void main(String[] args) {
        arguments = args;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println(Arrays.toString(arguments));
        System.exit(0);
    }
}
