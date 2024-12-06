package com.quynhlm.dev.be.core.exception;

public class NotificationNotFoundException extends RuntimeException{
    public NotificationNotFoundException (String message) {
        super(message);
    }
}
