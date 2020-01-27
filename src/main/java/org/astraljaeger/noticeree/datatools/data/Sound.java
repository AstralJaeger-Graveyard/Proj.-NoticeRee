package org.astraljaeger.noticeree.datatools.data;

import javafx.beans.property.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@SuppressWarnings("unused")
public class Sound {

    private static final Logger logger = LogManager.getLogger(Sound.class);

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

    public Sound(String username, int priority, String label, boolean nsfw, File originalFile){
        this(username, priority, label, nsfw);
        logger.debug("Initializing sound with original file");
        this.originalFile.setValue(originalFile);
        if(originalFile.getName().endsWith(".wav")){
            this.originalFile.setValue(originalFile);
            this.file.setValue(originalFile);
        }else {
            // Convert mp3 to wav and store next to mp3
            // TODO: implement this https://stackoverflow.com/questions/14085199/mp3-to-wav-conversion-in-java
            this.originalFile.setValue(originalFile);
            logger.debug("Converting mp3 file to wav: {}", originalFile.getName());

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
