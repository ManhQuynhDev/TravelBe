package com.quynhlm.dev.be.core.exception;

public class UserWasAlreadyRequest extends RuntimeException{
    public UserWasAlreadyRequest (String message) {
        super(message);
    }
}
