package com.ezertech.backend.exception;

import java.time.LocalDateTime;

public record BookNotAvailableError(
        LocalDateTime timestamp,
        int status,
        String code,
        String message,
        String path,
        int reservationsAhead
) {
}