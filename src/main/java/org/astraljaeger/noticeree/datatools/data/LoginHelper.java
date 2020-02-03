package org.astraljaeger.noticeree.datatools.data;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LoginHelper {

    StringProperty token;
    BooleanProperty save;
    final String twitchIdentityProvidername;
    final CredentialManager credentialManager;
    OAuth2Credential credential;

    public LoginHelper(CredentialManager credentialManager, String twitchIdentityProvidername){
        token = new SimpleStringProperty();
        save = new SimpleBooleanProperty();
        this.credentialManager = credentialManager;
        this.twitchIdentityProvidername = twitchIdentityProvidername;
        this.credential = null;
    }

    public String getToken() {

        return token.get();
    }

    public StringProperty tokenProperty() {

        return token;
    }

    public boolean isSave() {

        return save.get();
    }

    public BooleanProperty saveProperty() {

        return save;
    }


    public String getTwitchIdentityProvidername() {

        return twitchIdentityProvidername;
    }

    public CredentialManager getCredentialManager() {

        return credentialManager;
    }

    public OAuth2Credential getCredential() {

        return credential;
    }

    public void setCredential(OAuth2Credential credential) {

        this.credential = credential;
    }

}
