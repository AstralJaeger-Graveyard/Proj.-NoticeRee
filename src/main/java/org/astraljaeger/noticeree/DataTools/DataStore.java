/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.DataTools;

public class DataStore {

    private static DataStore instance;
    private static final String FILE_NAME = "data.nosql";

    public static DataStore getInstance(){
        if(instance == null)
            instance = new DataStore();
        return instance;
    }

    private DataStore(){

    }

}
