/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;

public class Utils {

    private static final Logger logger = LogManager.getLogger(Utils.class);

    private Utils(){

    }

    public static Dialog<Pair<String, Boolean>> createLoginDialog(String errorMessage){

        Dialog<Pair<String, Boolean>> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Please enter your Twitch OAuth2 token.");
        ButtonType confirmationBtn = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(confirmationBtn);

        GridPane grid = new GridPane();
        grid.setVgap(5);
        grid.setHgap(5);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Token: "), 0, 0);
        PasswordField passwordField = new PasswordField();
        passwordField.setMinWidth(350);
        passwordField.setPromptText("oauth:...");
        grid.add(passwordField, 1, 0);
        CheckBox saveToken = new CheckBox("save token");
        grid.add(saveToken, 1, 1);
        Hyperlink link = new Hyperlink("You can get a token at twitchapps.com/tmi");
        link.setOnAction(event -> {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if(desktop.isSupported(Desktop.Action.BROWSE)){
                    try {
                        desktop.browse(new URI("https://www.twitchapps.com/tmi"));
                    }catch (Exception ignored){}
                }
            }
        });
        grid.add(link, 0, 2, 2, 1);
        link.setAlignment(Pos.CENTER);

        if(!errorMessage.equals("")){
            Label errLabel = new Label(errorMessage);
            errLabel.setTextFill(Color.RED);
            grid.add(errLabel, 0, 3, 2, 1);
        }

        Node loginBtn = dialog.getDialogPane().lookupButton(confirmationBtn);
        loginBtn.setDisable(true);
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginBtn.setDisable(!(newValue.trim().length() > 29));
        });
        Platform.runLater(passwordField::requestFocus);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton ->{
            if(dialogButton == confirmationBtn){
                return new Pair<>(passwordField.getText(), saveToken.isSelected());
            }else {
                return null;
            }
        });
        return dialog;
    }

    public static Alert createErrorDialog(Exception e, String title, String header){
        Alert errorAlert = new Alert(AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(header);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stacktrace = sw.toString();
        Label headerLbl = new Label("The Stacktrace was");
        TextArea exceptionText = new TextArea(stacktrace);
        GridPane pane = new GridPane();
        exceptionText.setEditable(false);
        exceptionText.setWrapText(true);
        exceptionText.setMaxHeight(Double.MAX_VALUE);
        exceptionText.setMaxWidth(Double.MAX_VALUE);
        GridPane.setVgrow(exceptionText, Priority.ALWAYS);
        GridPane.setHgrow(exceptionText, Priority.ALWAYS);
        pane.setMaxWidth(Double.MAX_VALUE);
        pane.add(headerLbl, 0, 0);
        pane.add(exceptionText, 0, 1);
        errorAlert.getDialogPane().setExpandableContent(pane);
        return errorAlert;
    }

    public static void openUriInBrowser(String uri){
        if (isDesktopSupported()) {
            var desktop = getDesktop();
            if(desktop.isSupported(Desktop.Action.BROWSE)){
                try {
                    desktop.browse(new URI(uri));
                } catch (Exception e) {
                    logger.error("Not able to open uri {} browser: \n{}: {}", uri, e.getClass().getSimpleName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Method attempts to wait for specified time. Will continue normal procedures if interrupted
     * @param time amount to wait
     * @param unit timeunit to wait
     */
    public static void tryToWait(long time, TimeUnit unit){
        try {
            Thread.sleep(unit.toMillis(time));
        }catch (Exception ignored){
            logger.debug("Got interruped");
        }
    }
}
