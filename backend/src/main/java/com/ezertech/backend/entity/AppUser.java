package com.ezertech.backend.entity;

import lombok.*;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    // Siempre hasheada con BCrypt, nunca texto plano.
    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.BIBLIOTECARIO;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    public boolean isBlocked() {
        return blockedUntil != null && blockedUntil.isAfter(LocalDateTime.now());
    }
}
