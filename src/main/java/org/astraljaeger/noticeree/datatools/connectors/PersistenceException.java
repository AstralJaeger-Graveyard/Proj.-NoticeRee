/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.datatools.connectors;

public class PersistenceException extends RuntimeException {

    public PersistenceException(){
        super();
    }

    public PersistenceException(String message){
        super(message);
    }
}
