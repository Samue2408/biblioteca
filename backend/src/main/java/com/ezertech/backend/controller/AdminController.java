package com.ezertech.backend.controller;

import com.ezertech.backend.dto.admin.AdminStatsResponse;
import com.ezertech.backend.dto.admin.BlockedUserResponse;
import com.ezertech.backend.dto.loan.LoanResponse;
import com.ezertech.backend.entity.Loan;
import com.ezertech.backend.service.AdminStatsService;
import com.ezertech.backend.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminStatsService adminStatsService;
    private final LoanService loanService;

    public AdminController(AdminStatsService adminStatsService, LoanService loanService) {
        this.adminStatsService = adminStatsService;
        this.loanService = loanService;
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

    @GetMapping("/loans")
    public ResponseEntity<List<LoanResponse>> loans() {
        return ResponseEntity.ok(loanService.findAllLoans().stream()
                .map(this::toLoanResponse)
                .toList());
    }

    private LoanResponse toLoanResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getTitle(),
                loan.getBorrower().getEmail(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate());
    }
}
