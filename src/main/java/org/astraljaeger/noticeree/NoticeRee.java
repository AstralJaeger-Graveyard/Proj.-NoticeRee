/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree;

import java.util.Arrays;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.astraljaeger.noticeree.Controllers.MainController;

public class NoticeRee extends Application {

    private static String[] arguments;

    public static void main(String[] args) {
        arguments = args;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println(Arrays.toString(arguments));

        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
            final Parent root = loader.load();
            MainController controller = loader.getController();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Proj. NoticeRee");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (Exception e){
            System.exit(1);
        }
    }
}
