package org.astraljaeger.noticeree.datatools.data;

import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.List;

public class Chatter {

    private final IntegerProperty id;

    private final StringProperty username;

    private final StringProperty welcomeMessage;

    private final ListProperty<String> sounds;

    private final LongProperty lastUsed;

    public Chatter(int id){
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty();
        this.welcomeMessage = new SimpleStringProperty();
        this.sounds = new SimpleListProperty<>();
        this.lastUsed = new SimpleLongProperty();
    }

    public Chatter(int id, String username){
        this(id);
        this.username.setValue(username);
    }

    public Chatter(int id, String username, String welcomeMessage){
        this(id, username);
        this.welcomeMessage.setValue(welcomeMessage);
    }

    public Chatter(int id, String username, String welcomeMessage, List<String> sounds){
        this(id, username, welcomeMessage);
        this.sounds.addAll(sounds);
    }

    public Chatter(int id, String username, String welcomeMessage, List<String> sounds, long lastUsed){
        this(id, username, welcomeMessage, sounds);
        this.lastUsed.setValue(lastUsed);
    }

    public int getId() {

        return id.get();
    }

    public IntegerProperty idProperty() {

        return id;
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

    public ObservableList<String> getSounds() {

        return sounds.get();
    }

    public ListProperty<String> soundsProperty() {

        return sounds;
    }

    public long getLastUsed() {

        return lastUsed.get();
    }

    public LongProperty lastUsedProperty() {

        return lastUsed;
    }

    public String toString(){
        return id + ": " + username;
    }
}