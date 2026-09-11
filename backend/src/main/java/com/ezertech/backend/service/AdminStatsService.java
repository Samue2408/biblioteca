package com.ezertech.backend.service;

import com.ezertech.backend.dto.admin.AdminStatsResponse;
import com.ezertech.backend.dto.admin.BlockedUserResponse;
import com.ezertech.backend.entity.AppUser;
import com.ezertech.backend.entity.BookStatus;
import com.ezertech.backend.exception.ResourceNotFoundException;
import com.ezertech.backend.repository.AppUserRepository;
import com.ezertech.backend.repository.BookRepository;
import com.ezertech.backend.repository.LoanRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminStatsService {
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final AppUserRepository appUserRepository;

    public AdminStatsService(BookRepository bookRepository, LoanRepository loanRepository, AppUserRepository appUserRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.appUserRepository = appUserRepository;
    }

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                bookRepository.count(),
                bookRepository.countByStatus(BookStatus.DISPONIBLE),
                bookRepository.countByStatus(BookStatus.PRESTADO),
                bookRepository.countByStatus(BookStatus.RESERVADO),
                loanRepository.count(),
                loanRepository.countByReturnDateIsNull(),
                loanRepository.countByReturnDateIsNullAndDueDateBefore(LocalDate.now()),
                appUserRepository.countByBlockedUntilAfter(LocalDateTime.now())
        );
    }

    public List<BlockedUserResponse> getBlockedUsers() {
        return appUserRepository.findByBlockedUntilAfter(LocalDateTime.now()).stream()
                .map(u -> new BlockedUserResponse(u.getId(), u.getName(), u.getEmail(), u.getBlockedUntil()))
                .toList();
    }

    @Transactional
    public void unblockUser(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        user.setBlockedUntil(null);
        appUserRepository.save(user);
    }

}
