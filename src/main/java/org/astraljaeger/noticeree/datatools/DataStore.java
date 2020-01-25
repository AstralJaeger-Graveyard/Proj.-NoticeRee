/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.datatools;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.datatools.data.Chatter;
import org.dizitart.no2.Nitrite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    @Getter
    private ObservableList<Chatter> chattersList;

    private DataStore(){

        if(Configuration.USE_PERSISTANCE) {
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

            db = Nitrite.builder()
                    .filePath(Configuration.getAppDataDirectory() + FILE_NAME)
                    .openOrCreate();
        }
        chattersList = FXCollections.observableArrayList();
    }

    public synchronized Chatter addChatter(){
        Chatter item = new Chatter(chattersList.size());
        chattersList.add(item);
        return item;
    }

    public synchronized void addChatter(Chatter chatter){
        chattersList.add(chatter);
    }

    public synchronized void removeChatter(Chatter chatter){
        chattersList.remove(chatter);
    }

    public synchronized void updateChatter(Chatter updated){
        // TODO: update
    }

    public void close(){
        // TODO: Close db
    }
}
