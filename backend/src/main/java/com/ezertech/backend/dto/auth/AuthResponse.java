package com.ezertech.backend.dto.auth;

import com.ezertech.backend.entity.Role;

public record AuthResponse(String token, Role s) {
}
