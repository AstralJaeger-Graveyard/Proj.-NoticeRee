package org.astraljaeger.noticeree.DataTools;

public class CryptoException extends Exception {

    public CryptoException(){
    }

    public CryptoException(String message){
        super(message);
    }

    public CryptoException(String message, Throwable inner){
        super(message, inner);
    }
}
