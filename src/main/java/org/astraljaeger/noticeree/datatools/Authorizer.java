/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.datatools;

public class Authorizer {

    private static Authorizer instance;

    public static Authorizer getInstance(){
        if(instance == null)
            instance = new Authorizer();
        return instance;
    }

    private Authorizer(){

    }
}
