package org.astraljaeger.noticeree.controllers;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.Utils;
import org.astraljaeger.noticeree.datatools.data.LoginHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class LoginController {

    private static Logger logger = LogManager.getLogger(LoginController.class);

    @FXML
    public GridPane layoutPane;
    @FXML
    public Button loginBtn;
    @FXML
    public CheckBox saveTokenBox;
    @FXML
    public PasswordField tokenBox;
    @FXML
    public Hyperlink descriptionLbl;
    @FXML
    public ProgressIndicator loginIndicator;

    private List<Node> controls;

    private ExecutorService executorService;
    private TwitchClient client;
    private CredentialManager credentialManager;
    private TwitchIdentityProvider identityProvider;

    private final Pattern pattern = Pattern.compile("^(oauth:)([a-z\\d]{30})");
    private Stage stage;

    public LoginController(){
        executorService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("login-thread-%d").build());
        credentialManager = CredentialManagerBuilder.builder()
            .build();
        identityProvider = new TwitchIdentityProvider(Configuration.CLIENT_ID, "", "");
        credentialManager.registerIdentityProvider(identityProvider);
    }

    @FXML
    public void initialize(){
        logger.info("Initializing LoginWindow");

        loginBtn.setDisable(true);
        loginBtn.setOnAction(event -> {
            probeToken();
        });

        descriptionLbl.setOnAction(event -> Utils.openUriInBrowser("https://twitchapps.com/tmi/"));

        tokenBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if(pattern.matcher(newValue).matches()){
                logger.info("Token syntax: is valid.");
                loginBtn.setDisable(false);
            }else {
                logger.info("Token sxntax: invalid. ({}/{} chars long)", newValue.length(), 36);
                loginBtn.setDisable(true);
            }
        });

        saveTokenBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("save token setting changed to {}", newValue);
        });

        hideErrorLabel();
        hideLoadingIndicator();
        controls = new ArrayList<>();
        controls.addAll(layoutPane.getChildren());

    }

    public void setStage(Stage stage){
        this.stage = stage;
    }

    private void probeToken(){
        long start = System.nanoTime();
        logger.info("Start token probing");
        disableControls();
        showLoadingIndicator();

        // submit login task to executor service
        executorService.submit(()->{
            logger.info("Starting parallel probe");
            Optional<OAuth2Credential> credentialOptional = Optional.empty();
            try {
                OAuth2Credential inputCredentials = new OAuth2Credential(
                    identityProvider.getProviderName(), tokenBox.getText());
                credentialOptional = identityProvider
                    .getAdditionalCredentialInformation(inputCredentials);
            }catch (Exception e){
                logger.fatal("{}: {}\n{}", e.getClass().getSimpleName(), e.getMessage(), e.getCause());
            }

            logger.info("Login took: {}ms",(System.nanoTime()-start)/1000000.0);
            if(credentialOptional.isPresent()){
                // Build client and handoff
                logger.info("Probing successful! Welcome {} \n" +
                        "Creating client, cleaning up and handing over control.",
                    credentialOptional.get().getUserName());
                hideErrorLabel();
                buildTwitchClient(credentialOptional.get());
            }else {
                // show error:
                logger.info("Probing unsuccessful!");
                hideLoadingIndicator();
                enableControls();
                showErrorLabel("Please enter a valid token!");
            }

        });
    }

    private void disableControls(){
        Platform.runLater(() -> controls.forEach(n -> n.setDisable(true)));
    }

    private void enableControls(){
        Platform.runLater(()-> controls.forEach(n -> n.setDisable(false)));
    }

    private void showLoadingIndicator(){
        layoutPane.add(loginIndicator, 0, 0, 2, 5);
    }

    private void hideLoadingIndicator(){
        layoutPane.getChildren().remove(loginIndicator);
    }

    private void showErrorLabel(String errorMessage){
        Platform.runLater(()->{
            descriptionLbl.setText(errorMessage);
            descriptionLbl.setStyle("-fx-text-fill: red;");
        });
    }

    private void hideErrorLabel(){
        Platform.runLater(()->{
            descriptionLbl.setText("Get a token here");
            descriptionLbl.setStyle("-fx-text-fill: blue;");
        });
    }

    public void buildTwitchClient(OAuth2Credential checkedCredential){
        Platform.runLater(() -> {
            logger.info("Building TwitchClient:\nchat:true\nhelix:true\nkraken:true\npubsub:true\nevent-threads:4");
            client = TwitchClientBuilder.builder()
                .withCredentialManager(credentialManager)
                .withChatAccount(checkedCredential)
                .withEnableChat(true)
                .withEnableHelix(true)
                .withEnableKraken(true)
                .withEnablePubSub(true)
                .withEventManagerThreads(4)
                .build();

            logger.info("Handing off control, closing");
            close();
        });
    }

    public TwitchClient getClient(){
        return client;
    }

    public void close() {
        executorService.shutdown();
        logger.info("Awaiting executor shutdown");
        while (!executorService.isShutdown()){
            Utils.tryToWait(10, TimeUnit.MILLISECONDS);
        }
        logger.info("Executor shut down. Handoff NOW");
        stage.close();
    }
}
