package org.astraljaeger.noticeree.Controllers;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.storage.TemporaryStorageBackend;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.astraljaeger.noticeree.DataTools.ConfigStore;
import org.astraljaeger.noticeree.DataTools.DataStore;
import org.astraljaeger.noticeree.Utils;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MainController {

    private final Logger logger = Logger.getLogger(MainController.class.getSimpleName());

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

    @FXML
    public Button testAudioBtn;

    @FXML
    public Hyperlink usernameLink;

    @FXML
    public Hyperlink channelLink;

    @FXML
    public ChoiceBox<Object> audioOutputCb;

    @FXML
    public TextField channelTf;

    Stage primaryStage;
    DataStore store;
    TwitchClient client;

    DataStore dataStore;

    Mixer.Info device;

    private final String CLIENT_ID = "i76h7g9dys23tnsp4q5qbc9vezpwfb";

    public MainController(){

    }

    @FXML
    public void initialize(){

        logger.fine("Starting login process");
        client = doLogin();

        logger.fine("Setting up UI, restoring old config");
        setUiFromConfig();

        logger.fine("Inizialising database manager");
        dataStore = DataStore.getInstance();

        if(primaryStage != null){
            primaryStage.onCloseRequestProperty().addListener(((observable, oldValue, newValue) -> {
                // TODO: Close db and things
                logger.info("Terminating application");
                client.getChat().disconnect();
                store.close();
                Platform.exit();
            }));
        }

        mainTp.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            logger.info(String.format("Changed tab from '%d' to '%d'",
                    mainTp.getTabs().indexOf(oldValue),
                    mainTp.getTabs().indexOf(newValue)));
            // TODO: surely there is something to do here
        });

        audioOutputCb.getSelectionModel().selectedItemProperty().addListener((observable) -> {
            // TODO: play the test sound every time the output device is changed on that device
        });
    }

    private TwitchClient doLogin() {

        CredentialManager credentialManager = CredentialManagerBuilder.builder()
                .withStorageBackend(new TemporaryStorageBackend())
                .build();

        TwitchIdentityProvider twitchIdentityProvider = new TwitchIdentityProvider(CLIENT_ID, "", "");
        credentialManager.registerIdentityProvider(twitchIdentityProvider);
        String provider = twitchIdentityProvider.getProviderName();
        ConfigStore store = ConfigStore.getInstance();

        OAuth2Credential credential = null;
        OAuth2Credential result = null;
        String errorMessage = "";

        if(!store.getToken().equals("")){
            // get token from store
            String token = store.getToken();
            credential = new OAuth2Credential(provider, token);
            Optional<OAuth2Credential> storeOptional = twitchIdentityProvider.getAdditionalCredentialInformation(credential);
            if(storeOptional.isPresent()){
                result = storeOptional.get();
            }else {
                errorMessage = "Stored token is invalid, please re-enter";
            }
        }

        while(result == null){
            Dialog<Pair<String, Boolean>> dialog = Utils.createLoginDialog(errorMessage);
            Optional<Pair<String, Boolean>> pairOptional = dialog.showAndWait();
            String token = "";
            boolean save = false;

            if(pairOptional.isPresent()){
                Pair<String, Boolean> pair = pairOptional.get();
                token = pair.getKey();
                save = pair.getValue();
            }

            credential = new OAuth2Credential(provider, token);
            Optional<OAuth2Credential> loginOptional = twitchIdentityProvider.getAdditionalCredentialInformation(credential);
            if(loginOptional.isPresent()){
                result = loginOptional.get();
                if(save){
                    store.setToken(token);
                }
            }else {
                errorMessage = "Please enter a valid token";
            }
        }

       logger.info("Welcome " + result.getUserName());

        store.setUsername(result.getUserName());


        return TwitchClientBuilder.builder()
                .withCredentialManager(credentialManager)
                .withChatAccount(credential)
                .withEnableChat(true)
                .withEnableHelix(true)
                .withEnablePubSub(true)
                .build();
    }

    public void setUiFromConfig(){
        ConfigStore store = ConfigStore.getInstance();

        // Set links in about
        usernameLink.setText(store.getUsername());
        usernameLink.setOnAction(event -> {
            openUriInBrowser("https://www.twitch.tv/" + usernameLink.getText());
        });

        channelLink.setText(store.getChannel());
        channelLink.setOnAction(event -> {
            openUriInBrowser("https://www.twitch.tv/" + channelLink.getText());
        });

        channelTf.setText(store.getChannel());
        channelTf.textProperty().addListener(((observable, oldValue, newValue) -> {
            logger.fine("Setting channel name to " + newValue);
            channelTf.setText(newValue);
            store.setChannel(newValue);
        }));

        // Get list of audio devices and set configured device
        List<Mixer> devices = getPlaybackDevices();
        if(devices.size() != 0) {
            audioOutputCb.setItems(FXCollections.observableArrayList(devices));
            audioOutputCb.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
                device = ((Mixer) newValue).getMixerInfo();
                logger.info("Setting new playback device");
                store.setDefaultOutputDevice(device);
            }));

            // Restore playback device config
            Mixer.Info configuredMixer = store.getDefaultOutputDevice();
            if(configuredMixer != null) {
                int index = findMixerInfo(devices, configuredMixer);
                if (index != -1) {
                    logger.info(String.format("Found stored playback device: [%d] %s", index, configuredMixer));
                    audioOutputCb.getSelectionModel().select(index);
                }
            }else {
                logger.info("No playback device set, defaulting to 1st found device: " + ((Mixer)audioOutputCb.getItems().get(0)).getMixerInfo().getName());
                audioOutputCb.getSelectionModel().select(0);
            }
        }else {
            // TODO: flag error that no output devices were found
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
                .map(info -> "\t - " + info.getName() + " <> " + info.getDescription())
                .collect(Collectors.joining("\n"));
        logger.info("Found devices [" + results.size() + "]: \n" + devices);
        return results;
    }

    private void playTestSound(Mixer.Info mixerInfo){

        // TODO: play sound on selected mixer
    }

    private int findMixerInfo(List<Mixer> infos, Mixer.Info target){
        int i = 0;
        for(Mixer mixer:infos){
            Mixer.Info info = mixer.getMixerInfo();
            if(info.getName().equals(target.getName()) && info.getDescription().equals(target.getDescription())){
                return i;
            }
            i++;
        }
        return -1;
    }

    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }

    private void openUriInBrowser(String uri){
//        if (Desktop.isDesktopSupported()) {
//            Desktop desktop = Desktop.getDesktop();
//            if(desktop.isSupported(Desktop.Action.BROWSE)){
//                try {
//                    desktop.browse(new URI(uri));
//                }catch (Exception ignored){}
//            }
//        }
    }
}
