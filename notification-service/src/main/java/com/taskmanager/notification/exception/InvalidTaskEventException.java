package com.taskmanager.notification.exception;

public class InvalidTaskEventException extends RuntimeException {

    public InvalidTaskEventException(String message) {
        super(message);
    }

    public InvalidTaskEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
