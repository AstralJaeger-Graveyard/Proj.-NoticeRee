package org.astraljaeger.noticeree.Controllers;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.storage.TemporaryStorageBackend;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.astraljaeger.noticeree.DataTools.ConfigStore;
import org.astraljaeger.noticeree.DataTools.DataStore;
import org.astraljaeger.noticeree.Utils;
import org.checkerframework.checker.nullness.Opt;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

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

    @FXML
    public Hyperlink usernameLink;

    @FXML
    public Hyperlink channelLink;

    DataStore store;
    TwitchClient client;

    private final String CLIENT_ID = "i76h7g9dys23tnsp4q5qbc9vezpwfb";

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
            // TODO: surely there is some


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

        usernameLink.setText(credential.getUserName());
        usernameLink.setOnAction(event -> {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if(desktop.isSupported(Desktop.Action.BROWSE)){
                    try {
                        desktop.browse(new URI("https://www.twitch.tv/" + usernameLink.getText()));
                    }catch (Exception ignored){}
                }
            }
        });

        channelLink.setText(store.getChannel());
        channelLink.setOnAction(event -> {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if(desktop.isSupported(Desktop.Action.BROWSE)){
                    try {
                        desktop.browse(new URI("https://www.twitch.tv/" + channelLink.getText()));
                    }catch (Exception ignored){}
                }
            }
        });

        return TwitchClientBuilder.builder()
                .withCredentialManager(credentialManager)
                .withChatAccount(credential)
                .withEnableChat(true)
                .withEnableHelix(true)
                .withEnablePubSub(true)
                .build();
    }

    public void setPrimaryStage(Stage primaryStage){

    }
}
