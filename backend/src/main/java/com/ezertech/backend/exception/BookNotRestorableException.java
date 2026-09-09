package com.ezertech.backend.exception;

public class BookNotRestorableException extends RuntimeException {
    public BookNotRestorableException(String message) {
        super(message);
    }
}
