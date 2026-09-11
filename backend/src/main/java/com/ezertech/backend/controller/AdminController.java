package com.ezertech.backend.controller;

import com.ezertech.backend.dto.admin.AdminStatsResponse;
import com.ezertech.backend.dto.admin.BlockedUserResponse;
import com.ezertech.backend.service.AdminStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminStatsService adminStatsService;

    public AdminController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminStatsService.getStats());
    }

    @GetMapping("/users/blocked")
    public ResponseEntity<List<BlockedUserResponse>> blockedUsers() {
        return ResponseEntity.ok(adminStatsService.getBlockedUsers());
    }

    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable Long id) {
        adminStatsService.unblockUser(id);
        return ResponseEntity.noContent().build();
    }
}
