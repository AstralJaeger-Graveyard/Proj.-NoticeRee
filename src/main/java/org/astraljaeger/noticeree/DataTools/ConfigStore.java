/*
 * Copyright (c) 2020.
 */

package org.astraljaeger.noticeree.DataTools;

import org.astraljaeger.noticeree.Configuration;

public class TokenStore {

    private static TokenStore instance;

    public static TokenStore getInstance(){
        if(instance == null)
            instance = new TokenStore();
        return instance;
    }

    private String location = Configuration.CONFIG_LOCATION;

    private TokenStore(){

    }
}
