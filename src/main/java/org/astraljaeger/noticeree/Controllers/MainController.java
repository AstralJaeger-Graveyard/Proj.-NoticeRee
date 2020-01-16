package org.astraljaeger.noticeree.Controllers;

import com.github.twitch4j.TwitchClient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.astraljaeger.noticeree.DataTools.DataStore;

public class MainController {

    @FXML
    public TabPane mainTp;

    @FXML
    public TableView welcomeMsgTv;

    @FXML
    public TableView noticeMeTv;

    @FXML
    public TableView chattersTv;

    @FXML
    public Button addWelcomeMsgBtn;

    @FXML
    public Button removeWelcomeMsgBtn;

    @FXML
    public Button addNoticeMeBtn;

    @FXML
    public Button removeNoticeMeBtn;

    @FXML
    public Button playNoticeMeBtn;

    @FXML
    public Button stopNoticeMeBtn;

    @FXML
    public Button applyFilterNoticeMeBtn;

    DataStore store;
    TwitchClient client;

    public MainController(){

    }

    @FXML
    public void initialize(){

        client = doLogin();

        mainTp.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Changed tab from '" +
                    mainTp.getTabs().indexOf(oldValue) +
                    "' to '" +
                    mainTp.getTabs().indexOf(newValue) +
                    "'");



        });


        

    }

    private TwitchClient doLogin() {


        return null;
    }

    public void setPrimaryStage(Stage primaryStage){

    }
}
