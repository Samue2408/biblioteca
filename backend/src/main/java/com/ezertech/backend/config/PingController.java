package com.ezertech.backend.config;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ezertech.backend.entity.AppUser;

@RestController
public class PingController {

    @GetMapping("/api/ping")
    public String ping(@AuthenticationPrincipal AppUser user) {
        return "Hola " + user.getEmail() + ", tu rol es " + user.getRole();
    }
}
