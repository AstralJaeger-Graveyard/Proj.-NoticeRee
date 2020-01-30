package org.astraljaeger.noticeree.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.astraljaeger.noticeree.Utils;
import org.astraljaeger.noticeree.datatools.data.LoginHelper;

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

    private final Pattern pattern = Pattern.compile("^(oauth:)([a-z\\d]{30})");
    private Stage stage;

    @FXML
    public void initialize(){
        logger.info("Initializing LoginWindow");
        loginBtn.setDisable(true);
        getTokenLink.setOnAction(event -> Utils.openUriInBrowser("https://twitchapps.com/tmi/"));
        loginBtn.setOnAction(event -> {
            logger.info("Handing control back to caller");
            stage.close();
        });
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

    public void setStage(Stage stage){
        this.stage = stage;
    }
}
