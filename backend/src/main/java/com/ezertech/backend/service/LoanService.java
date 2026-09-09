package com.ezertech.backend.service;

import com.ezertech.backend.entity.*;
import com.ezertech.backend.event.BookAvailableEvent;
import com.ezertech.backend.event.LoanCreatedEvent;
import com.ezertech.backend.event.UserBlockedEvent;
import com.ezertech.backend.exception.BookNotAvailableException;
import com.ezertech.backend.exception.LoanAlreadyReturnedException;
import com.ezertech.backend.exception.ResourceNotFoundException;
import com.ezertech.backend.exception.UserBlockedException;
import com.ezertech.backend.repository.AppUserRepository;
import com.ezertech.backend.repository.BookRepository;
import com.ezertech.backend.repository.LoanRepository;
import com.ezertech.backend.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class LoanService {
    private static final int LOAN_DAYS = 14;
    private static final int MAX_DELAYS = 3;
    private static final int BLOCK_WEEKS = 1;


    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final AppUserRepository appUserRepository;
    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       AppUserRepository appUserRepository,
                       ReservationRepository reservationRepository,
                       ApplicationEventPublisher eventPublisher) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.appUserRepository = appUserRepository;
        this.reservationRepository = reservationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Loan createLoan(Long bookId, AppUser borrower) {
        if (borrower.isBlocked()) {
            throw new UserBlockedException(
                    "Tu cuenta está bloqueada hasta " + borrower.getBlockedUntil());
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        if (book.getStatus() != BookStatus.DISPONIBLE) {
            long ahead = reservationRepository.countByBookAndStatus(book, ReservationStatus.PENDIENTE);
            throw new BookNotAvailableException(
                    "El libro '" + book.getTitle() + "' no está disponible", (int) ahead);
        }

        LocalDate today = LocalDate.now();

        Loan loan = Loan.builder()
                .book(book)
                .borrower(borrower)
                .loanDate(today)
                .dueDate(today.plusDays(LOAN_DAYS))
                .build();

        book.setStatus(BookStatus.PRESTADO);
        bookRepository.save(book);

        Loan savedLoan = loanRepository.save(loan);

        eventPublisher.publishEvent(new LoanCreatedEvent(savedLoan.getId()));

        return savedLoan;
    }

    @Transactional
    public Loan returnLoan(Long loanId) {
        Loan loan = loanRepository.findWithBookAndBorrowerById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado"));

        if (loan.getReturnDate() != null) {
            throw new LoanAlreadyReturnedException("Este préstamo ya fue devuelto");
        }

        LocalDate today = LocalDate.now();
        boolean isLate = today.isAfter(loan.getDueDate());
        loan.setReturnDate(today);

        // se guarda antes de contar, para que este atraso quede dentro de los 90 días
        Loan savedLoan = loanRepository.save(loan);

        if (isLate) {
            applyDelayPenalty(loan.getBorrower());
        }

        releaseOrReserveBook(loan.getBook());

        return savedLoan;
    }

    public List<Loan> findLoansByBorrower(AppUser borrower) {
        return loanRepository.findByBorrowerWithBookAndBorrower(borrower);
    }

    private void applyDelayPenalty(AppUser borrower) {
        LocalDate since = LocalDate.now().minusDays(90);
        long lateReturnsInWindow = loanRepository.countLateReturnsSince(borrower, since);

        if (lateReturnsInWindow >= MAX_DELAYS) {
            borrower.setBlockedUntil(LocalDateTime.now().plusWeeks(BLOCK_WEEKS));
            appUserRepository.save(borrower);
            eventPublisher.publishEvent(new UserBlockedEvent(borrower.getId()));
        }
    }

    private void releaseOrReserveBook(Book book) {
        Optional<Reservation> next = reservationRepository
                .findFirstByBookAndStatusOrderByRequestDateAsc(book, ReservationStatus.PENDIENTE);

        if (next.isPresent()) {
            Reservation reservation = next.get();
            reservation.setStatus(ReservationStatus.NOTIFICADO);
            reservationRepository.save(reservation);

            book.setStatus(BookStatus.RESERVADO);
            eventPublisher.publishEvent(new BookAvailableEvent(reservation.getId()));
        } else {
            book.setStatus(BookStatus.DISPONIBLE);
        }

        bookRepository.save(book);
    }


}
