package org.astraljaeger.noticeree.datatools.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LoginHelper {

    StringProperty token;
    BooleanProperty save;

    public LoginHelper(){
        token = new SimpleStringProperty();
        save = new SimpleBooleanProperty();
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

}
