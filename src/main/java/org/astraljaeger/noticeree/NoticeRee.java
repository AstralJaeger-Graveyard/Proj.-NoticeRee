/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.astraljaeger.noticeree.controllers.MainController;

public class NoticeRee extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private MainController controller;

    @Override
    public void start(Stage primaryStage) {

        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
            final Parent root = loader.load();
            controller = loader.getController();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Proj. NoticeRee");
            primaryStage.setScene(scene);
            controller.setPrimaryStage(primaryStage);
            primaryStage.show();
        }
        catch (Exception e){
            System.exit(1);
        }
    }

    @Override
    public void stop(){
        controller.stop();
    }
}
