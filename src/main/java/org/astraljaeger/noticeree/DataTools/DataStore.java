/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.DataTools;

import org.astraljaeger.noticeree.Configuration;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;

public class DataStore {

    private static DataStore instance;
    private static final String FILE_NAME = "data.nosql";
    private static final String WELCOME_MSG_COLLECTION = "welcomeMsgCollection";
    private static final String NOTICE_ME_COLLECTION = "noticeMeCollection";


    public static DataStore getInstance(){
        if(instance == null)
            instance = new DataStore();
        return instance;
    }

    private final Nitrite db;
    private final NitriteCollection welcomeMsgColletion;
    private final NitriteCollection noticeMeCollection;

    private DataStore(){

        db = Nitrite.builder()
                .filePath(Configuration.getAppDataDirectory() + FILE_NAME)
                .openOrCreate();

        welcomeMsgColletion = db.getCollection(WELCOME_MSG_COLLECTION);
        noticeMeCollection = db.getCollection(NOTICE_ME_COLLECTION);
    }

    public void addWelcomeMsg(){

    }


}
