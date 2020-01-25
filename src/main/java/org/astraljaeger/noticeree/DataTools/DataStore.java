/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.DataTools;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.DataTools.Data.Chatter;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class DataStore {

    private static final Logger logger = Logger.getLogger(DataStore.class.getSimpleName());
    private static DataStore instance;
    private static final String FILE_NAME = "data.db";

    public static DataStore getInstance(){
        if(instance == null)
            instance = new DataStore();
        return instance;
    }

    private Nitrite db;
    private ObjectRepository<Chatter> chatterRepository;
    public ObservableList<Chatter> chattersList;

    private DataStore(){

        if(!Files.exists(Paths.get(Configuration.getAppDataDirectory()))){
            try {
                Files.createDirectory(Paths.get(Configuration.getAppDataDirectory()));
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        db = Nitrite.builder()
                .filePath(Configuration.getAppDataDirectory() + FILE_NAME)
                .openOrCreate();
        chatterRepository = db.getRepository(Chatter.class);
        chatterRepository.register(info -> {
            switch (info.getChangeType()){
                case INSERT:
                    logger.info("Inserting " + info.getChangedItems().size() + " Element");
                    break;
                case UPDATE:
                    logger.info("Updating " + info.getChangedItems().size() + " Element");
                    break;
                case REMOVE:
                    logger.info("Removing " + info.getChangedItems().size() + " Element");
                    break;
                default:
                    break;
            }
        });


        chattersList = FXCollections.observableArrayList();
        Cursor<Chatter> cursor = chatterRepository.find();
        for(Chatter chatter : cursor){
            chattersList.add(chatter);
        }
    }

    public void addChatter(Chatter chatter){
        chatterRepository.insert(chatter);
        chattersList.add(chatter);
    }

    public void removeChatter(Chatter chatter){

    }

    public void updateChatter(){

    }

    public void close(){
        if(db != null && !db.isClosed()){
            db.close();
        }
    }


}
