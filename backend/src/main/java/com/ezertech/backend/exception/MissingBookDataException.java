package com.ezertech.backend.exception;

public class MissingBookDataException extends RuntimeException {
    public MissingBookDataException(String message) {
        super(message);
    }
}