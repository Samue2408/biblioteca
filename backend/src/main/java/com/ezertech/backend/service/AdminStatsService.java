package com.ezertech.backend.service;

import com.ezertech.backend.dto.admin.AdminStatsResponse;
import com.ezertech.backend.entity.BookStatus;
import com.ezertech.backend.repository.BookRepository;
import com.ezertech.backend.repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AdminStatsService {
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public AdminStatsService(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                bookRepository.count(),
                bookRepository.countByStatus(BookStatus.DISPONIBLE),
                bookRepository.countByStatus(BookStatus.PRESTADO),
                bookRepository.countByStatus(BookStatus.RESERVADO),
                loanRepository.count(),
                loanRepository.countByReturnDateIsNull(),
                loanRepository.countByReturnDateIsNullAndDueDateBefore(LocalDate.now())
        );
    }
}
