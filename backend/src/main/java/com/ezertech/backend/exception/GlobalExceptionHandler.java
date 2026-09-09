package com.ezertech.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleEmailInUse(EmailAlreadyInUseException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "EMAIL_ALREADY_IN_USE", ex.getMessage(), req);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Credenciales inválidas", req);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(BookNotAvailableException.class)
    public ResponseEntity<BookNotAvailableError> handleBookNotAvailable(BookNotAvailableException ex, HttpServletRequest req) {
        BookNotAvailableError body = new BookNotAvailableError(
                LocalDateTime.now(), HttpStatus.CONFLICT.value(), "BOOK_NOT_AVAILABLE",
                ex.getMessage(), req.getRequestURI(), ex.getReservationsAhead());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ApiError> handleUserBlocked(UserBlockedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "USER_BLOCKED", ex.getMessage(), req);
    }

    @ExceptionHandler(LoanAlreadyReturnedException.class)
    public ResponseEntity<ApiError> handleAlreadyReturned(LoanAlreadyReturnedException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "LOAN_ALREADY_RETURNED", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateReservationException.class)
    public ResponseEntity<ApiError> handleDuplicateReservation(DuplicateReservationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "DUPLICATE_RESERVATION", ex.getMessage(), req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, HttpServletRequest req) {
        ApiError body = ApiError.of(status.value(), code, message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

}
