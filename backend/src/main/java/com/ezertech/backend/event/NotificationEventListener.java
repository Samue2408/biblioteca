package com.ezertech.backend.event;

import com.ezertech.backend.service.NotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

public class NotificationEventListener {
    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLoanCreated(LoanCreatedEvent event) {
        notificationService.sendLoanConfirmation(event.loanId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserBlocked(UserBlockedEvent event) {
        notificationService.sendAccountBlocked(event.userId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookAvailable(BookAvailableEvent event) {
        notificationService.sendBookAvailable(event.reservationId());
    }
}
