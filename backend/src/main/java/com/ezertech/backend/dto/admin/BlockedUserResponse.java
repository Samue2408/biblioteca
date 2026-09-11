package com.ezertech.backend.dto.admin;

import java.time.LocalDateTime;

public record BlockedUserResponse(Long id, String name, String email, LocalDateTime blockedUntil) {
}
