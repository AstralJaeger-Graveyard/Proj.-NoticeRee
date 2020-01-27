package org.astraljaeger.noticeree.controllers;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.astraljaeger.noticeree.datatools.data.Chatter;
import org.astraljaeger.noticeree.datatools.data.Sound;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class EditorController {

    private static final Logger logger = LogManager.getLogger(EditorController.class);

    // region FXML fields
    @FXML
    public Button lastUsedResetBtn;

    @FXML
    public Button addSoundBtn;

    @FXML
    public Button removeSoundBtn;

    @FXML
    public Button priorityUpBtn;

    @FXML
    public Button priorityDownBtn;

    @FXML
    public Label lastUsedLbl;

    @FXML
    public TextField usernameTb;

    @FXML
    public TextField welcomeMsgTb;

    @FXML
    public TableView<Sound> soundsTv;

    @FXML
    public TableColumn<Sound, String> priorityCol;

    @FXML
    public TableColumn<Sound, String> labelCol;

    @FXML
    public TableColumn<Sound, String> fileCol;

    // endregion

    private Stage primaryStage;

    @FXML
    public void initialize(){
        soundsTv.setPlaceholder(new Label("Such empty, much wow!"));
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

        bindTableView(chatter);
        bindButtonEvents(chatter);
    }

    private void bindTableView(Chatter chatter){
        soundsTv.setItems(chatter.getSounds());
        soundsTv.setEditable(true);

        priorityCol.setCellValueFactory(cellData -> cellData.getValue().priorityProperty().asString());
        priorityCol.setCellFactory(TextFieldTableCell.forTableColumn());
        priorityCol.setOnEditCommit((TableColumn.CellEditEvent<Sound, String> event) -> {
            try {
                event.getRowValue()
                        .priorityProperty()
                        .setValue(Integer.parseInt(event.getNewValue()));
            }catch (NumberFormatException e){
                logger.error("Invalid number format: {} {}", e.getClass().getSimpleName(), e.getMessage());
            }
        });

        labelCol.setCellValueFactory(new PropertyValueFactory<>("label"));
        labelCol.setCellFactory(TextFieldTableCell.forTableColumn());
        labelCol.setOnEditCommit((TableColumn.CellEditEvent<Sound, String> event) -> event.getRowValue()
                .labelProperty()
                .set(event.getNewValue()));

        fileCol.setCellValueFactory(cellData -> cellData.getValue().fileProperty().asString());
        fileCol.setCellFactory(TextFieldTableCell.forTableColumn());
        fileCol.setOnEditCommit((TableColumn.CellEditEvent<Sound, String> event) -> {
            File newFile = new File(event.getNewValue());
            if(newFile.exists()) {
                event.getRowValue()
                        .fileProperty()
                        .setValue(new File(event.getNewValue()));
            }else
                logger.error("New file does not exist");
        });
    }

    private void bindButtonEvents(Chatter chatter){
        lastUsedResetBtn.setOnAction(event -> {
            chatter.lastUsedProperty().setValue(LocalDateTime.of(2020, 1, 1, 0,0,0));
            logger.debug("Resetting last used property");
        });

        addSoundBtn.setOnAction(event -> {
            List<File> files = createChooser().showOpenMultipleDialog(primaryStage.getOwner());
            if(files != null && !files.isEmpty()){
                for(File f : files){
                    String fileUri = f.getAbsoluteFile().toURI().toString();
                    chatter.soundsProperty().add(new Sound(
                            chatter.getUsername(),
                            chatter.soundsProperty().size(),
                            fileUri.substring(fileUri.lastIndexOf('/')),
                            (fileUri.contains("nsfw")),
                            f));

                    logger.debug("Added sound {}", fileUri);
                }
            }else
                logger.error("Adding sound(s) failed");

        });

        removeSoundBtn.setOnAction(event -> {
            int toRemove = soundsTv.getSelectionModel().getSelectedIndex();
            logger.debug("Removing sound: {}", soundsTv.getSelectionModel().getSelectedItem());
            if(toRemove >= 0){
                chatter.soundsProperty().remove(toRemove);
            }
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
