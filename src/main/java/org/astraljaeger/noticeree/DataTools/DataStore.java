/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.DataTools;

import org.astraljaeger.noticeree.Configuration;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
    private List<Chatter> chatters;

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


        chatters = new ArrayList<>(102);
        Cursor<Chatter> cursor = chatterRepository.find();
        for(Chatter chatter : cursor){
            chatters.add(chatter);
        }
    }

    public void addChatter(Chatter chatter){
        
    }

    public void close(){
        if(db != null && !db.isClosed()){
            db.close();
        }
    }


}
