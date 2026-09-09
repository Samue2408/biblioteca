package com.ezertech.backend.service;

import com.ezertech.backend.dto.auth.AuthResponse;
import com.ezertech.backend.dto.auth.LoginRequest;
import com.ezertech.backend.dto.auth.RegisterRequest;
import com.ezertech.backend.entity.AppUser;
import com.ezertech.backend.entity.Role;
import com.ezertech.backend.exception.EmailAlreadyInUseException;
import com.ezertech.backend.repository.AppUserRepository;
import com.ezertech.backend.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException("Ya existe una cuenta con ese correo");
        }

        AppUser user = AppUser.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.BIBLIOTECARIO)
                .build();

        appUserRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user.getEmail()), Role.BIBLIOTECARIO);
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        return new AuthResponse(jwtService.generateToken(user.getEmail()), user.getRole());
    }
}
