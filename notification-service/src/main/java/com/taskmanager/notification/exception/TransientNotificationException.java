package com.taskmanager.notification.exception;

public class TransientNotificationException extends RuntimeException {

    public TransientNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
