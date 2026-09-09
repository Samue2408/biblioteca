package com.ezertech.backend.exception;

public class LoanAlreadyReturnedException extends RuntimeException {
    public LoanAlreadyReturnedException(String message) {
        super(message);
    }
}
