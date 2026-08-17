package com.taskmanager.exception;

import java.util.UUID;

public class SubtaskNotFoundException extends RuntimeException {

    public SubtaskNotFoundException(UUID taskId, UUID subtaskId) {
        super("Subtask " + subtaskId + " was not found for task " + taskId);
    }
}
