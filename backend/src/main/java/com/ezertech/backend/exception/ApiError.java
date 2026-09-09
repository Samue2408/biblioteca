package com.ezertech.backend.exception;

import java.time.LocalDateTime;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String code,
        String message,
        String path
) {
    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, code, message, path);
    }
}