package org.astraljaeger.noticeree.datatools.data;

import javafx.beans.property.*;

import java.io.File;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Sound {

    private final static Logger logger = Logger.getLogger(Sound.class.getSimpleName());

    private final String username;
    private final IntegerProperty priority;
    private final StringProperty label;
    private final BooleanProperty nsfw;
    private final ObjectProperty<File> file;
    private final ObjectProperty<File> originalFile;

    public Sound(String username){
        this.username = username;
        priority = new SimpleIntegerProperty();
        label = new SimpleStringProperty();
        nsfw = new SimpleBooleanProperty(false);
        file = new SimpleObjectProperty<>();
        originalFile = new SimpleObjectProperty<>();
    }

    public Sound(String username, int priority){
        this(username);
        this.priority.setValue(priority);
    }

    public Sound(String username, int priority, String label){
        this(username, priority);
        this.label.setValue(label);
    }

    public Sound(String username, int priority, String label, boolean nsfw){
        this(username, priority, label);
        this.nsfw.setValue(nsfw);
    }

    public Sound(String username, int priority, String label, boolean nsfw, File file, File originalFile){
        this(username, priority, label, nsfw);
        this.originalFile.setValue(originalFile);
        if(file != null){
            this.file.setValue(file);
        }else {
            // Convert mp3 to wav and store next to mp3
            // TODO: implement this https://stackoverflow.com/questions/14085199/mp3-to-wav-conversion-in-java
        }
    }

    public String getUsername() {

        return username;
    }

    public int getPriority() {

        return priority.get();
    }

    public IntegerProperty priorityProperty() {

        return priority;
    }

    public void setPriority(int priority) {

        this.priority.set(priority);
    }

    public String getLabel() {

        return label.get();
    }

    public StringProperty labelProperty() {

        return label;
    }

    public void setLabel(String label) {

        this.label.set(label);
    }

    public boolean isNsfw() {

        return nsfw.get();
    }

    public BooleanProperty nsfwProperty() {

        return nsfw;
    }

    public void setNsfw(boolean nsfw) {

        this.nsfw.set(nsfw);
    }

    public File getFile() {

        return file.get();
    }

    public ObjectProperty<File> fileProperty() {

        return file;
    }

    public void setFile(File file) {

        this.file.set(file);
    }

    public File getOriginalFile() {

        return originalFile.get();
    }

    public ObjectProperty<File> originalFileProperty() {

        return originalFile;
    }

    public void setOriginalFile(File originalFile) {

        this.originalFile.set(originalFile);
    }

    public boolean hasLabel(){
        return !label.get().equals("");
    }

    @Override
    public String toString() {
        if(!hasLabel())
            return priority.get() + " " + file.get().toString() + " " + (nsfw.get() ? "[NSFW]" : "");

        return priority.get() + " " + label.get() + " " + (nsfw.get() ? "[NSFW]" : "");
    }

}
