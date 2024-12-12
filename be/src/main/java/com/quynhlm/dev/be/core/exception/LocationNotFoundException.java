package com.quynhlm.dev.be.core.exception;

public class LocationNotFoundException extends RuntimeException{
    public LocationNotFoundException(String message){
        super(message);
    }
}
