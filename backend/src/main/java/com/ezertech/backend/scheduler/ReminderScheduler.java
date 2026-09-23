package com.ezertech.backend.scheduler;

import com.ezertech.backend.entity.Loan;
import com.ezertech.backend.repository.LoanRepository;
import com.ezertech.backend.service.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderScheduler {
    private final LoanRepository loanRepository;
    private final NotificationService notificationService;

    public ReminderScheduler(LoanRepository loanRepository, NotificationService notificationService) {
        this.loanRepository = loanRepository;
        this.notificationService = notificationService;
    }

    // @Scheduled(cron = "0 */1 * * * *")
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendDueSoonReminders() {
        LocalDate today = LocalDate.now();
        List<Loan> dueSoon = loanRepository
                .findByReturnDateIsNullAndReminderSentAtIsNullAndDueDateBetween(today, today.plusDays(2));

        for (Loan loan : dueSoon) {
            notificationService.sendDueSoonReminder(loan);
            loan.setReminderSentAt(LocalDateTime.now());
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendOverdueNotices() {
        List<Loan> overdue = loanRepository
                .findByReturnDateIsNullAndDueDateBeforeAndOverdueNoticeSentAtIsNull(LocalDate.now());

        for (Loan loan : overdue) {
            notificationService.sendOverdueNotice(loan);
            loan.setOverdueNoticeSentAt(LocalDateTime.now());
        }
    }
}
