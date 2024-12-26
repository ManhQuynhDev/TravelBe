package com.quynhlm.dev.be.core.exception;

public class AccountIsDisabledException extends RuntimeException{
    public AccountIsDisabledException(String message) {
        super(message);
    }
}
