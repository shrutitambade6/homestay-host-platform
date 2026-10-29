package com.homestay.platform.service;

/** Raised when a status change is not allowed (wrong current status or wrong role). */
public class WorkflowException extends RuntimeException {
    public WorkflowException(String message) {
        super(message);
    }
}
