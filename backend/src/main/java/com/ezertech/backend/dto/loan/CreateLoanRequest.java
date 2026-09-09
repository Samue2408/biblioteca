package com.ezertech.backend.dto.loan;

import jakarta.validation.constraints.NotNull;

public record CreateLoanRequest(@NotNull Long bookId) {
}
