package org.astraljaeger.noticeree;

import freetimelabs.io.reactorfx.flux.FxFlux;
import freetimelabs.io.reactorfx.schedulers.FxSchedulers;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML
    public TabPane mainTp;

    public MainController(){

    }

    @FXML
    public void initialize(){

        mainTp.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Changed tab from \'" +
                    mainTp.getTabs().indexOf(oldValue) +
                    "\' to \'" +
                    mainTp.getTabs().indexOf(newValue) +
                    "\'");



        });

    }

    public void setPrimaryStage(Stage primaryStage){

    }
}
