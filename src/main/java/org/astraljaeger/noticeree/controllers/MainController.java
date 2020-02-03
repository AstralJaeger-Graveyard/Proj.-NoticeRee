package org.astraljaeger.noticeree.controllers;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.Utils;
import org.astraljaeger.noticeree.datatools.ConfigStore;
import org.astraljaeger.noticeree.datatools.DataStore;
import org.astraljaeger.noticeree.datatools.data.Chatter;
import org.astraljaeger.noticeree.datatools.data.MixerHelper;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
public class MainController {

    private static final Logger logger = LogManager.getLogger(MainController.class);
    private static final String BASE_URI = "https://www.twitch.tv/";

    // region FXML Fields
    @FXML
    public TabPane mainTp;

    @FXML
    public TableView<Chatter> chattersTv;

    @FXML
    public TableColumn<Chatter, String> chattersUsernameCol;

    @FXML
    public TableColumn<Chatter, String> chattersMessageCol;

    @FXML
    public TableColumn<Chatter, String> chattersSoundsCol;

    @FXML
    public TableColumn<Chatter, String> chattersLastUsedCol;

    @FXML
    public Button addBtn;

    @FXML
    public Button removeBtn;

    @FXML
    public Button editBtn;

    @FXML
    public Button playBtn;

    @FXML
    public Button stopBtn;

    @FXML
    public Button testAudioBtn;

    @FXML
    public Hyperlink usernameLink;

    @FXML
    public Hyperlink channelLink;

    @FXML
    public ChoiceBox<MixerHelper> audioOutputCb;

    @FXML
    public TextField channelTf;
    // endregion

    Stage primaryStage;
    TwitchClient client;
    DataStore dataStore;
    MixerHelper device;
    LoginController loginController;

    public MainController(){

    }

    @FXML
    public void initialize(){

        logger.info("Pre-Init: Setting up scene events");
        setupSceneEvents();

        logger.info("Pre-Init: Inizialising persistance manager");
        dataStore = DataStore.getInstance();

        logger.info("Init: Binding data to view");
        setupTableView();

        logger.info("Post-Init: Starting login process");
        doLogin();

        logger.info("Post-Init: Restoring old config to UI components");
        setUiFromConfig();
    }

    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }

    // region setup

    private void doLogin() {

        client = openLoginWindow();
    }

    public void setUiFromConfig(){
        ConfigStore configStore = ConfigStore.getInstance();

        // Set links in about
        usernameLink.setText(configStore.getUsername());
        usernameLink.setOnAction(event -> Utils.openUriInBrowser(BASE_URI + usernameLink.getText()));

        channelLink.setText(configStore.getChannel());
        channelLink.setOnAction(event -> Utils.openUriInBrowser(BASE_URI + channelLink.getText()));

        channelTf.setText(configStore.getChannel());
        channelTf.textProperty().addListener(((observable, oldValue, newValue) -> {
            logger.info("Setting channel name to {}", newValue);
            channelTf.setText(newValue);
            configStore.setChannel(newValue);
        }));

        // Get list of audio devices and set configured device
        List<Mixer> devices = getPlaybackDevices();
        if(devices.isEmpty()) {

            // Collect devices
            audioOutputCb.setItems(FXCollections.observableArrayList(
                    devices.stream()
                            .map(mixer -> new MixerHelper(
                                    mixer.getMixerInfo().getName(),
                                    mixer.getMixerInfo().getDescription(),
                                    mixer))
                            .collect(Collectors.toList()))
            );

            // Device changed event
            audioOutputCb.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
                device = newValue;
                logger.info("Setting new playback device");
                playTestSound(device);
                configStore.setDefaultOutputDevice(device.getMixer().getMixerInfo());
            }));

            // Restore playback device config
            Mixer.Info configuredMixer = configStore.getDefaultOutputDevice();
            if(configuredMixer != null) {
                int index = findMixerInfo(audioOutputCb.getItems(), configuredMixer);
                if (index != -1) {
                    logger.info("Found stored playback device: [{}] {}}", index, configuredMixer);
                    audioOutputCb.getSelectionModel().select(index);
                }
            }
            else {
                // TODO: Consider giving a window to tell the user to configure a playback device
                logger.info("No playback device set, defaulting to 1st found device: {}", ((Mixer)audioOutputCb.getItems().get(0)).getMixerInfo().getName());
                audioOutputCb.getSelectionModel().select(0);
            }
        }
        else {
            // TODO: flag error that no output devices were found
            logger.info("No playback devices were found!");
        }
    }

    private List<Mixer> getPlaybackDevices(){
        logger.info("Gathering audio playback devices");
        Line.Info playbackLine = new Line.Info(SourceDataLine.class);
        List<Mixer> results;
        List<Mixer.Info> infos = Lists.newArrayList(AudioSystem.getMixerInfo());
        results = infos.stream()
                .map(AudioSystem::getMixer)
                .filter(mixer -> mixer.isLineSupported(playbackLine))
                .collect(Collectors.toList());
        String devices = results.stream()
                .map(Mixer::getMixerInfo)
                .map(info -> "\t - " + info.getName() + ": " + info.getDescription())
                .collect(Collectors.joining("\n"));
        logger.info("Found devices {}: \n {}", results.size(), devices);
        return results;
    }

    private void setupTableView(){
        logger.info("Setting up data bindings");
        chattersTv.setPlaceholder(new Label("Much empty! Such wow!"));
        chattersTv.setItems(DataStore.getInstance().getChattersList());
        chattersTv.setEditable(true);
        chattersUsernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        chattersUsernameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        chattersUsernameCol.setOnEditCommit((TableColumn.CellEditEvent<Chatter, String> t)->{
                t.getTableView()
                .getItems()
                .get(t.getTablePosition().getRow())
                .usernameProperty()
                .setValue(t.getNewValue());
                dataStore.updateChatter(t.getOldValue(), t.getRowValue());
        });
        chattersMessageCol.setCellValueFactory(new PropertyValueFactory<>("welcomeMessage"));
        chattersMessageCol.setCellFactory(TextFieldTableCell.forTableColumn());
        chattersMessageCol.setOnEditCommit((TableColumn.CellEditEvent<Chatter, String> t)->{
            t.getTableView()
                    .getItems()
                    .get(t.getTablePosition().getRow())
                    .welcomeMessageProperty()
                    .setValue(t.getNewValue());
            dataStore.updateChatter(t.getRowValue().getUsername(), t.getRowValue());
        });
        chattersSoundsCol.setCellValueFactory(new PropertyValueFactory<>("sounds"));
        chattersLastUsedCol.setCellValueFactory(new PropertyValueFactory<>("lastUsed"));

        // Set better renderer for username, lastUsed and sounds
    }

    private void setupSceneEvents(){
        logger.info("Setup program exit event");

        if(primaryStage != null){
            primaryStage.onCloseRequestProperty().addListener(((observable, oldValue, newValue) -> {
                terminate(0);
            }));
        }

        logger.info("Setup tab change event");
        mainTp.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                logger.info("Changed tab from '{}' to '{}'",
                        mainTp.getTabs().indexOf(oldValue),
                        mainTp.getTabs().indexOf(newValue))
        );

        logger.info("Setup add sound event");
        addBtn.setOnAction(event -> {
            Chatter chatter = new Chatter("NewUser");
            openEditorWindow(chatter, "Add new user");
            logger.info("Added chatter: {}", chatter);
            dataStore.addChatter(chatter);
        });

        logger.info("Setup edit sound event");
        editBtn.setOnAction(event -> {
            Chatter selected = chattersTv.getSelectionModel().getSelectedItem();
            if(selected != null){
                String oldUsername = selected.getUsername();
                openEditorWindow(selected, "Editing user " + oldUsername);
                logger.info("Updating chatter: {} to {}", oldUsername, (!oldUsername.equals(selected.getUsername()) ? "" : " to " + selected.getUsername()));
                dataStore.updateChatter(oldUsername, selected);
            }
        });

        logger.info("Setup remove sound event");
        removeBtn.setOnAction(event -> {
            // might wanna add a safety dialog here
            Chatter toRemove = chattersTv.getSelectionModel().getSelectedItem();
            if(toRemove != null){
                logger.info("Removing chatter: {}", toRemove);
                dataStore.removeChatter(toRemove);
            }
        });
    }

    // endregion
    // region class-bound utility

    private void playTestSound(MixerHelper mixerInfo){
        logger.info("Playing test sound on {}", mixerInfo);
        // TODO: play sound on selected mixer

    }

    private int findMixerInfo(List<MixerHelper> helpers, Mixer.Info target){
        int i = 0;
        for(MixerHelper helper: helpers){
            Mixer.Info info = helper.getMixer().getMixerInfo();
            if(info.getName().equals(target.getName()) && info.getDescription().equals(target.getDescription())){
                return i;
            }
            i++;
        }
        return -1;
    }

    private void openEditorWindow(Chatter chatter, String title){
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditorWindow.fxml"));
            final Parent root = loader.load();
            final EditorController controller = loader.getController();
            controller.bind(chatter);

            final Stage popupStage = new Stage();
            controller.setPrimaryStage(popupStage);
            popupStage.setScene(new Scene(root));
            popupStage.initOwner(primaryStage);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle(title);
            popupStage.showAndWait();
        }catch (IOException e){
            logger.info("Error opening editor window:\n {}}: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Opens a login window and retieves token
     * @return TwitchClient with authentified chat account or null (will terminate application in that case)
     */
    private TwitchClient openLoginWindow(){

        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginWindow.fxml"));
            final Parent root = loader.load();
            final LoginController controller = loader.getController();
            final Stage popupStage = new Stage();

            controller.setStage(popupStage);
            popupStage.setScene(new Scene(root));
            popupStage.initOwner(primaryStage);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Login");
            popupStage.showAndWait();
            return controller.getClient();

        }catch (IOException e){
            logger.fatal(
                "Error loading 'loginWindow.fxml': \n>Exception: {}: {}\n >Cause: {}",
                e.getClass().getSimpleName(),
                e.getMessage().replace('\n', (char)0),
                e.getCause()
            );
            terminate(1);
        }

        return null;
    }

    private void terminate(int code){
        if(code != 0){
            System.exit(code);
        }

        logger.info("Terminating application");
        client.getChat().disconnect();
        logger.info("Disconnected chat");
        dataStore.close();
        logger.info("Disconnect datasource");
        shutdownExecutorService();
        Platform.exit();
    }

    private void shutdownExecutorService(){
        // TODO: For now now executor is used, but one might be used
        logger.info("executor service is shutdown");
    }

    // endregion
}