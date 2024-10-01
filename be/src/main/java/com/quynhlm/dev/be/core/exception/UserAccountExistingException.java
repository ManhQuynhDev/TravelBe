package com.quynhlm.dev.be.core.exception;

public class UserAccountExistingException extends RuntimeException{
    public UserAccountExistingException(String message){
        super(message);
    }
}
