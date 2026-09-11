package com.ezertech.backend.controller;

import com.ezertech.backend.dto.auth.AuthResponse;
import com.ezertech.backend.dto.auth.LoginRequest;
import com.ezertech.backend.dto.auth.RegisterRequest;
import com.ezertech.backend.entity.AppUser;
import com.ezertech.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal AppUser user) {
        // El token no cambia, solo confirmamos quién es y su rol actual
        return ResponseEntity.ok(new AuthResponse(null, user.getRole().name()));
    }

}
