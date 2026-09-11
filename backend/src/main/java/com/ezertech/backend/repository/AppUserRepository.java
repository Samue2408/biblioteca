package com.ezertech.backend.repository;

import com.ezertech.backend.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AppUser> findByBlockedUntilAfter(LocalDateTime now);
    long countByBlockedUntilAfter(LocalDateTime now);
}
