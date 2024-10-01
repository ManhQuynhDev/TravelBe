package com.quynhlm.dev.be.core.exception;

public class UserAccountNotFoundException extends RuntimeException{
    public UserAccountNotFoundException (String message){
        super(message);
    }
}
