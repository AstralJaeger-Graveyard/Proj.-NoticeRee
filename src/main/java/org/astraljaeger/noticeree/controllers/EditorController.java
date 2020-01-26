package org.astraljaeger.noticeree.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.astraljaeger.noticeree.datatools.data.Chatter;

public class EditorController {

    @FXML
    public Button lastUsedResetBtn;

    @FXML
    public Button addSoundBtn;

    @FXML
    public Button removeSoundBtn;

    @FXML
    public Label lastUsedLbl;

    @FXML
    public TextField usernameTb;

    @FXML
    public TextField welcomeMsgTb;

    void bind(Chatter chatter){

        usernameTb.textProperty().bindBidirectional(chatter.usernameProperty());
        welcomeMsgTb.textProperty().bindBidirectional(chatter.welcomeMessageProperty());

    }


}
