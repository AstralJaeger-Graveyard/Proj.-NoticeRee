/*
 * Copyright (c) 2020.
 */

package org.astraljaeger.noticeree;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class Utils {

    public static Alert createErrorDialog(Exception e, String title, String header){
        Alert errorAlert = new Alert(AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(header);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stacktrace = sw.toString();
        Label headerLbl = new Label("The Stacktrace what");
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
}
