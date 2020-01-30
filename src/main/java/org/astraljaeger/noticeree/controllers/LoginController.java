package org.astraljaeger.noticeree.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.astraljaeger.noticeree.Utils;
import org.astraljaeger.noticeree.datatools.data.LoginHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class LoginController {

    private static Logger logger = LogManager.getLogger(LoginController.class);

    @FXML
    private Button loginBtn;
    @FXML
    private CheckBox saveTokenBox;
    @FXML
    private PasswordField tokenBox;
    @FXML
    private Hyperlink getTokenLink;
    @FXML
    private ProgressIndicator loginIndicator;

    private ExecutorService executorService;

    private final Pattern pattern = Pattern.compile("^(oauth:)([a-z\\d]{30})");
    private Stage stage;

    public LoginController(){
        executorService = Executors.newSingleThreadExecutor();
    }

    @FXML
    public void initialize(){
        loginIndicator.managedProperty().bind(loginIndicator.visibleProperty());
        loginIndicator.setManaged(false);

        logger.info("Initializing LoginWindow");
        loginBtn.setDisable(true);
        getTokenLink.setOnAction(event -> Utils.openUriInBrowser("https://twitchapps.com/tmi/"));
        loginBtn.setOnAction(event -> {
            logger.info("Handing control back to caller");
            stage.close();
        });
    }

    public void close() {
        executorService.shutdown();
        while (!executorService.isShutdown()){
            Utils.tryToWait(10, TimeUnit.MILLISECONDS);
        }
        stage.close();
    }

    public void bind(LoginHelper helper){

        logger.info("Binding helper to LoginController");
        helper.saveProperty().bindBidirectional(saveTokenBox.selectedProperty());
        tokenBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if(pattern.matcher(newValue).matches()){
                logger.info("Token syntax: valid");
                helper.tokenProperty().setValue(newValue);
                loginBtn.setDisable(false);
            }
            else {
                loginBtn.setDisable(true);
            }
        });
    }

    private void probeLogin(LoginHelper helper){

    }

    public void setStage(Stage stage){
        this.stage = stage;
    }
}
