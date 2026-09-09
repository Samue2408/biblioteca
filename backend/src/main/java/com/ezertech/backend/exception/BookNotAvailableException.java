package com.ezertech.backend.exception;

public class BookNotAvailableException extends RuntimeException {

    private final int reservationsAhead;

    public BookNotAvailableException(String message, int reservationsAhead) {
        super(message);
        this.reservationsAhead = reservationsAhead;
    }

    public int getReservationsAhead() {
        return reservationsAhead;
    }
}