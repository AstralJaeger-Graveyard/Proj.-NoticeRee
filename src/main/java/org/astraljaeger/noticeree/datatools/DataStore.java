/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.datatools;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.datatools.data.Chatter;
import org.astraljaeger.noticeree.datatools.data.Sound;
import org.dizitart.no2.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.IndexOptions.indexOptions;
import static org.dizitart.no2.filters.Filters.eq;

public class DataStore {

    private static final Logger logger = LogManager.getLogger(DataStore.class);
    private static final String FILE_NAME = "data.db";

    private static DataStore instance;

    public static DataStore getInstance(){
        if(instance == null)
            instance = new DataStore();
        return instance;
    }

    private Nitrite db;
    private NitriteCollection chatterCollection;
    private NitriteCollection soundCollection;

    @Getter
    private ObservableList<Chatter> chattersList;

    private static final String USERNAME = "username";
    private static final String WELCOME_MESSAGE = "welcomeMessage";
    private static final String LAST_USED = "lastUsed";

    private static final String PRIORITY = "priority";
    private static final String LABEL = "label";
    private static final String NSFW = "nsfw";
    private static final String FILE = "file";
    private static final String ORIGINAL_FILE = "originalFile";

    private DataStore(){
        chattersList = FXCollections.observableArrayList();

        if(Configuration.USE_PERSISTANCE) {
            checkOrCreateFolder();

            db = Nitrite.builder()
                    .filePath(Configuration.getAppDataDirectory() + FILE_NAME)
                    .openOrCreate();

            chatterCollection = db.getCollection(Chatter.class.getName());
            if(!isIndexed(chatterCollection))
                chatterCollection.createIndex(USERNAME, indexOptions(IndexType.Fulltext));

            soundCollection = db.getCollection(Sound.class.getName());
            if(!isIndexed(soundCollection))
                soundCollection.createIndex(USERNAME, indexOptions(IndexType.Fulltext));

            load();
        }
    }

    /**
     * This method adds a new chatter and corresponding sounds to the database
     * @param chatter the chatter to be added
     */
    public synchronized void addChatter(Chatter chatter){
        logger.info("Adding new chatter: {}", chatter);
        if(Configuration.USE_PERSISTANCE){
            Document chatterDocument = chatter2Doc(chatter);
            chatterCollection.insert(chatterDocument);
            for(Sound s : chatter.getSounds()){
                Document soundDocument = sound2Doc(s);
                soundCollection.insert(soundDocument);
            }
        }

        chattersList.add(chatter);
    }

    /**
     * This method removes a chatter and all to him bound sounds
     * @param chatter the chatter to be removed
     */
    public synchronized void removeChatter(Chatter chatter){
        logger.info("Removing chatter: {}", chatter);
        if(Configuration.USE_PERSISTANCE){
            chatterCollection.remove(eq(USERNAME, chatter.getUsername()));
            soundCollection.remove(eq(USERNAME, chatter.getUsername()));
        }
        chattersList.remove(chatter);
    }

    /**
     * This method removes the chatter from the database and add a new chatter to it.
     *
     * @param oldUsername the username of the chatter before the change
     * @param updated the updated object
     */
    public synchronized void updateChatter(String oldUsername, Chatter updated){
        logger.info("Updating chatter from {} to {}", oldUsername, updated.getUsername());
        if(Configuration.USE_PERSISTANCE){
            chatterCollection.remove(eq(USERNAME, oldUsername));
            soundCollection.remove(eq(USERNAME, oldUsername));
            chatterCollection.insert(chatter2Doc(updated));
            for(Sound s : updated.getSounds())
                soundCollection.insert(sound2Doc(s));
        }
    }

    /**
     * This method closes the connection to the database to ensure correct resource freeing
     */
    public void close(){
        logger.info("Closing database connection");
        if(Configuration.USE_PERSISTANCE && !db.isClosed()){
            db.close();
        }
    }

    private void checkOrCreateFolder(){
        if (!Files.exists(Paths.get(Configuration.getAppDataDirectory()))) {
            try {
                Files.createDirectory(Paths.get(Configuration.getAppDataDirectory()));
            } catch (IOException e) {
                logger.info("An error occurred while creating the data directory: {}",
                        Arrays.stream(e.getStackTrace())
                                .map(trace -> String.format(" at %s#%s(%s:%d)",
                                        trace.getClass().getName(),
                                        trace.getMethodName(),
                                        trace.getFileName(),
                                        trace.getLineNumber()))
                                .collect(Collectors.joining(",\n")));
            }
        }
    }

    private void load(){
        logger.debug("Trying to load stored data");
        for(Document chatterDoc : chatterCollection.find(FindOptions.sort(USERNAME, SortOrder.Ascending))){
            Chatter chatter = fromChatterDocument(chatterDoc);
            for(Document soundDoc : soundCollection.find(eq(USERNAME, chatter.getUsername()))){
                chatter.getSounds().add(fromSoundDocument(soundDoc));
            }
            chattersList.add(chatter);
        }
    }

    private Document chatter2Doc(Chatter chatter){
        return createDocument(USERNAME, chatter.getUsername())
                .put(WELCOME_MESSAGE, chatter.getWelcomeMessage())
                .put(LAST_USED, chatter.getLastUsed());
    }

    private Document sound2Doc(Sound sound){
        return createDocument(USERNAME, sound.getUsername())
                .put(PRIORITY, sound.getPriority())
                .put(LABEL, sound.getLabel())
                .put(NSFW, sound.isNsfw())
                .put(FILE, sound.getFile().toURI().toString())
                .put(ORIGINAL_FILE, sound.getOriginalFile().toURI().toString());
    }

    private Sound fromSoundDocument(Document doc){
        Integer priority = doc.get(PRIORITY, Integer.class);
        String username = doc.get(USERNAME, String.class);
        String label = doc.get(LABEL, String.class);
        Boolean nsfw = doc.get(NSFW, Boolean.class);
        File file = new File(doc.get(FILE, String.class));
        File originalFile = new File(doc.get(ORIGINAL_FILE, String.class));
        return new Sound(username, priority, label, nsfw, file, originalFile);
    }

    private Chatter fromChatterDocument(Document doc){
        String username = doc.get(USERNAME, String.class);
        String message = doc.get(WELCOME_MESSAGE, String.class);
        LocalDateTime lastUsed = doc.get(LAST_USED, LocalDateTime.class);
        return new Chatter(username, message, new ArrayList<>(), lastUsed);
    }

    private boolean isIndexed(NitriteCollection collection){
        Collection<Index> indices = collection.listIndices();
        for (Index index : indices) {
            if(index.getField().equals(USERNAME)){
                return true;
            }
        }
        return false;
    }
}
