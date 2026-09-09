package com.ezertech.backend.exception;

//Cuando falla la consulta
public class ExternalBookLookupException extends RuntimeException {
    public ExternalBookLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
