package com.ezertech.backend.dto.loan;

import java.time.LocalDate;

public record LoanResponse(
        Long id,
        String bookTitle,
        String borrowerEmail,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate
) {
}
