/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.datatools;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.datatools.data.Chatter;
import org.dizitart.no2.*;
import org.dizitart.no2.event.ChangedItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.IndexOptions.indexOptions;
import static org.dizitart.no2.filters.Filters.eq;

public class DataStore {

    private static final Logger logger = Logger.getLogger(DataStore.class.getSimpleName());
    private static final String FILE_NAME = "data.db";

    private static DataStore instance;

    public static DataStore getInstance(){
        if(instance == null)
            instance = new DataStore();
        return instance;
    }

    private Nitrite db;

    private NitriteCollection collection;

    @Getter
    private ObservableList<Chatter> chattersList;

    private static final String USERNAME = "username";
    private static final String WELCOME_MESSAGE = "welcomeMessage";
    private static final String SOUNDS = "sounds";
    private static final String LAST_USED = "lastUsed";

    private DataStore(){
        chattersList = FXCollections.observableArrayList();

        if(Configuration.USE_PERSISTANCE) {
            checkOrCreateFolder();

            db = Nitrite.builder()
                    .filePath(Configuration.getAppDataDirectory() + FILE_NAME)
                    .openOrCreate();

            collection = db.getCollection(Chatter.class.getName());

            if(!isIndexed(collection)){
                collection.createIndex(USERNAME, indexOptions(IndexType.Fulltext));
            }

            collection.register(changeInfo -> logger.fine(changeInfo.getChangeType().name() + " element(s) " +
                    changeInfo.getChangedItems()
                            .stream()
                            .map(ChangedItem::getDocument)
                            .map(doc -> doc.get(USERNAME).toString())
                            .collect(Collectors.joining(", "))));

            load();
        }
    }

    public synchronized void addChatter(Chatter chatter){
        logger.info("Adding new chatter: " + chatter);
        if(Configuration.USE_PERSISTANCE){
            Document doc = convertToDoc(chatter);
            collection.insert(doc);
        }

        chattersList.add(chatter);
    }

    public synchronized void removeChatter(Chatter chatter){
        logger.info("Removing chatter: " + chatter);
        if(Configuration.USE_PERSISTANCE){
            collection.remove(eq(USERNAME, chatter.getUsername()));
        }
        chattersList.remove(chatter);
    }

    public synchronized void updateChatter(Chatter old, Chatter updated){
        logger.info("Updading chatter from " + old + " to " + updated);
        if(Configuration.USE_PERSISTANCE){
            collection.remove(eq(USERNAME, old.getUsername()));
            collection.insert(convertToDoc(updated));
        }
    }

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
                logger.info("An error occurred while creating the data directory: " +
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
        Cursor results = collection.find(FindOptions.sort(USERNAME, SortOrder.Ascending));
        for(Document doc : results){
            String username = doc.get(USERNAME, String.class);
            String message = doc.get(WELCOME_MESSAGE, String.class);
            List<String> sounds = new ArrayList<>();
            if(doc.get(SOUNDS, List.class) != null) {
                for(Object o: doc.get(SOUNDS, List.class))
                    if(o instanceof String)
                        sounds.add((String)o);
            }
            Long lastUsed = doc.get(LAST_USED, Long.class);
            chattersList.add(new Chatter(username, message, sounds, lastUsed));
        }
    }

    private Document convertToDoc(Chatter chatter){
        return createDocument(USERNAME, chatter.getUsername())
                .put(WELCOME_MESSAGE, chatter.getWelcomeMessage())
                .put(SOUNDS, chatter.getSounds())
                .put(LAST_USED, chatter.getLastUsed());
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
