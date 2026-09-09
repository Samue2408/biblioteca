package com.ezertech.backend.exception;

public class BookNotDeletableException extends RuntimeException {
    public BookNotDeletableException(String message) {
        super(message);
    }
}
