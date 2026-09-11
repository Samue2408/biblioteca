package com.ezertech.backend.dto.admin;

public record AdminStatsResponse(
        long totalBooks,
        long availableBooks,
        long borrowedBooks,
        long reservedBooks,
        long totalLoans,
        long activeLoans,
        long overdueLoans,
        long blockedUsers
) {
}
