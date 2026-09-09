package com.ezertech.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ResponseEntity<ApiError> handleBookNotAvailable(BookNotAvailableException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "BOOK_NOT_AVAILABLE", ex.getMessage(), req);
    }

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ApiError> handleUserBlocked(UserBlockedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "USER_BLOCKED", ex.getMessage(), req);
    }

    @ExceptionHandler(LoanAlreadyReturnedException.class)
    public ResponseEntity<ApiError> handleAlreadyReturned(LoanAlreadyReturnedException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "LOAN_ALREADY_RETURNED", ex.getMessage(), req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, HttpServletRequest req) {
        ApiError body = ApiError.of(status.value(), code, message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

}
