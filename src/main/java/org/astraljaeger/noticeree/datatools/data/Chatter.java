package org.astraljaeger.noticeree.datatools.data;

import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class Chatter implements Cloneable{
    private final StringProperty username;

    private final StringProperty welcomeMessage;

    private final ListProperty<String> sounds;

    private final LongProperty lastUsed;

    public Chatter(){
        this.username = new SimpleStringProperty();
        this.welcomeMessage = new SimpleStringProperty();
        this.sounds = new SimpleListProperty<>();
        this.lastUsed = new SimpleLongProperty();
    }

    public Chatter(String username){
        this();
        this.username.setValue(username);
    }

    public Chatter(String username, String welcomeMessage){
        this(username);
        this.welcomeMessage.setValue(welcomeMessage);
    }

    public Chatter(String username, String welcomeMessage, List<String> sounds){
        this(username, welcomeMessage);
        this.sounds.addAll(sounds);
    }

    public Chatter(String username, String welcomeMessage, List<String> sounds, long lastUsed){
        this(username, welcomeMessage, sounds);
        this.lastUsed.setValue(lastUsed);
    }

    public Chatter(Chatter other){
        this(other.username.getValue(),
                other.welcomeMessage.getValue(),
                new ArrayList<String>(other.sounds),
                other.lastUsed.getValue());
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
        return username.get();
    }
}