package org.astraljaeger.noticeree.controllers;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.astraljaeger.noticeree.datatools.data.Chatter;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class EditorController {

    private static final Logger logger = Logger.getLogger(EditorController.class.getSimpleName());

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

    @FXML
    public ListView<String> soundsLv;

    private Stage primaryStage;

    @FXML
    public void initialize(){
        soundsLv.setPlaceholder(new Label("Such empty, much wow!"));
    }

    void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
        Platform.runLater(()->{
            usernameTb.requestFocus();
            usernameTb.selectAll();
        });
    }

    void bind(Chatter chatter){
        usernameTb.textProperty().bindBidirectional(chatter.usernameProperty());
        welcomeMsgTb.textProperty().bindBidirectional(chatter.welcomeMessageProperty());
        lastUsedLbl.textProperty().bind(chatter.lastUsedProperty().asString());
        soundsLv.setItems(chatter.getSounds());

        lastUsedResetBtn.setOnAction(event -> {
            chatter.lastUsedProperty().setValue(0);
            logger.fine("Resetting last used property");
        });

        addSoundBtn.setOnAction(event -> {
            List<File> files = createChooser().showOpenMultipleDialog(primaryStage.getOwner());
            if(!files.isEmpty()){
                for(File f : files){
                    String fileUri = f.getAbsoluteFile().toURI().toString();
                    if(fileUri.endsWith(".mp3")){
                        // TODO: convert file
                        fileUri = fileUri.replace(".mp3", "_autoconverted.wav");
                        chatter.soundsProperty().add(fileUri);
                        logger.fine("Adding new converted sound: " + fileUri);
                    }else {

                        chatter.soundsProperty().add(fileUri);
                        logger.fine("Adding new sound: " + fileUri);
                    }
                }
            }
        });

        removeSoundBtn.setOnAction(event -> {
            int toRemove = soundsLv.getSelectionModel().getSelectedIndex();
            if(toRemove >= 0){
                chatter.soundsProperty().remove(toRemove);
            }
            logger.fine("Removing sound");
        });

    }


    private FileChooser createChooser(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select .wav or .mp3 sound to be played");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported audio files", "*.mp3", "*.wav"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Wave files (*.wav)", "*.wav"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 files (*.mp3)", "*.mp3"));
        return fileChooser;
    }

}
