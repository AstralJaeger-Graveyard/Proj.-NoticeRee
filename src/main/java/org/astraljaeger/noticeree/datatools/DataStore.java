/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.datatools;

import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.datatools.connectors.IConnection;
import org.astraljaeger.noticeree.datatools.connectors.NitriteConnectionFactory;
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

    private static DataStore instance;

    public static DataStore getInstance(){
        if(instance == null)
            instance = new DataStore();
        return instance;
    }

    @Getter
    private ObservableList<Chatter> chattersList;

    IConnection datasource;

    private DataStore(){
        chattersList = FXCollections.observableArrayList();
        datasource = new NitriteConnectionFactory().getConnection();
        chattersList.addAll(datasource.getChatters());
    }

    /**
     * This method adds a new chatter and corresponding sounds to the database
     * @param chatter the chatter to be added
     */
    public synchronized void addChatter(Chatter chatter){
        logger.info("Adding new chatter: {}", chatter);

        datasource.addChatter(chatter);
        chattersList.add(chatter);
    }

    /**
     * This method removes a chatter and all to him bound sounds
     * @param chatter the chatter to be removed
     */
    public synchronized void removeChatter(Chatter chatter){
        logger.info("Removing chatter: {}", chatter);

        datasource.removeChatter(chatter);
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
        datasource.updateChatter(oldUsername, updated);

        for(int i = 0; i < chattersList.size(); i++){
            if(chattersList.get(i).getUsername().equals(oldUsername)){
                chattersList.remove(i);
                break;
            }
        }
        chattersList.add(updated);
    }

    // TODO: Query from database not from list.
    //  This will break when pagination is introduced.
    public synchronized Optional<Chatter> findChatter(String username){
        return getChattersList()
            .stream()
            .filter(chatter->chatter.getUsername().equals(username))
            .findFirst();
    }

    /**
     * This method closes the connection to the database to ensure correct resource freeing
     */
    public void close(){
        logger.info("Closing database connection");
        chattersList.clear();
        datasource.close();
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

}
