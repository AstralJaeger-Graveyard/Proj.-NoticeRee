/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.DataTools;

public class ChatterStore {

    private static ChatterStore instance;

    public static ChatterStore getInstance(){
        if(instance == null)
            instance = new ChatterStore();
        return instance;
    }

    private ChatterStore(){

    }
}
