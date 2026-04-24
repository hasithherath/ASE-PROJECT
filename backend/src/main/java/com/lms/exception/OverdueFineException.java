package com.lms.exception;

public class OverdueFineException extends RuntimeException {
    public OverdueFineException(String message) {
        super(message);
    }
}
