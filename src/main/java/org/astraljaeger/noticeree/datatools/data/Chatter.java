package org.astraljaeger.noticeree.datatools.data;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
public class Chatter{
    private final StringProperty username;

    private final StringProperty welcomeMessage;

    private final ListProperty<Sound> sounds;

    private final ObjectProperty<LocalDateTime> lastUsed;

    public Chatter(){
        this.username = new SimpleStringProperty();
        this.welcomeMessage = new SimpleStringProperty();
        this.sounds = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.lastUsed = new SimpleObjectProperty<>(LocalDateTime.of(2020,1,1,0,0,0));
    }

    public Chatter(String username){
        this();
        this.username.setValue(username);
    }

    public Chatter(String username, String welcomeMessage){
        this(username);
        this.welcomeMessage.setValue(welcomeMessage);
    }

    public Chatter(String username, String welcomeMessage, List<Sound> sounds){
        this(username, welcomeMessage);
        this.sounds.addAll(sounds);
    }

    public Chatter(String username, String welcomeMessage, List<Sound> sounds, LocalDateTime lastUsed){
        this(username, welcomeMessage, sounds);
        this.lastUsed.setValue(lastUsed);
    }

    public String getUsername() {

        return username.get();
    }

    public StringProperty usernameProperty() {

        return username;
    }

    public String getWelcomeMessage() {

        return welcomeMessage.get();
    }

    public StringProperty welcomeMessageProperty() {

        return welcomeMessage;
    }

    public ObservableList<Sound> getSounds() {

        return sounds.get();
    }

    public ListProperty<Sound> soundsProperty() {

        return sounds;
    }

    public LocalDateTime getLastUsed() {

        return lastUsed.get();
    }

    public ObjectProperty<LocalDateTime> lastUsedProperty() {

        return lastUsed;
    }

    public String toString(){
        return username.get();
    }
}