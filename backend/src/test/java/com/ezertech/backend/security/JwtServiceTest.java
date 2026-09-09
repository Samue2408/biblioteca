package com.ezertech.backend.security;

import com.ezertech.backend.config.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            new JwtProperties("clave-de-prueba-con-al-menos-32-caracteres!!", 60_000));

    @Test
    void generatesAndValidatesToken() {
        String token = jwtService.generateToken("usuario@test.com");

        assertThat(token).isNotBlank();
        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractSubject(token)).isEqualTo("usuario@test.com");
    }

    @Test
    void invalidTokenIsRejected() {
        assertThat(jwtService.isValid("esto.no.es.un.token")).isFalse();
    }
}
