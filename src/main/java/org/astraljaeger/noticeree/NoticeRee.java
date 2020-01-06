/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree;

import java.util.Arrays;
import javafx.application.Application;
import javafx.stage.Stage;
import org.astraljaeger.noticeree.DataTools.ConfigStore;
import org.astraljaeger.noticeree.DataTools.KeyStore;

public class NoticeRee extends Application {

    private static String[] arguments;

    public static void main(String[] args) {
        arguments = args;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println(Arrays.toString(arguments));

        ConfigStore store = ConfigStore.getInstance();
        store.setToken("oauth:zzyyl9236vdhssmw79kbvos7n7l1si");


        System.exit(0);
    }
}
