package com.ezertech.backend.controller;

import com.ezertech.backend.dto.loan.CreateLoanRequest;
import com.ezertech.backend.dto.loan.LoanResponse;
import com.ezertech.backend.entity.AppUser;
import com.ezertech.backend.entity.Loan;
import com.ezertech.backend.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ResponseEntity<LoanResponse> create(@Valid @RequestBody CreateLoanRequest request,
                                               @AuthenticationPrincipal AppUser borrower) {
        Loan loan = loanService.createLoan(request.bookId(), borrower);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(loan));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponse> returnLoan(@PathVariable Long id) {
        Loan loan = loanService.returnLoan(id);
        return ResponseEntity.ok(toResponse(loan));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<LoanResponse>> mine(@AuthenticationPrincipal AppUser borrower) {
        List<LoanResponse> loans = loanService.findLoansByBorrower(borrower)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(loans);
    }

    private LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getTitle(),
                loan.getBorrower().getEmail(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate()
        );
    }
}
