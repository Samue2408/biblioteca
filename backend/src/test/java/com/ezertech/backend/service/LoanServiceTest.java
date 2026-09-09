package com.ezertech.backend.service;

import com.ezertech.backend.entity.*;
import com.ezertech.backend.exception.BookNotAvailableException;
import com.ezertech.backend.exception.UserBlockedException;
import com.ezertech.backend.repository.AppUserRepository;
import com.ezertech.backend.repository.BookRepository;
import com.ezertech.backend.repository.LoanRepository;
import com.ezertech.backend.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private BookRepository bookRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private AppUserRepository appUserRepository;
    @Mock private ReservationRepository reservationRepository;

    private LoanService loanService;

    @Test
    void throwsWhenBookIsNotAvailable() {
        loanService = new LoanService(loanRepository, bookRepository, appUserRepository, reservationRepository, eventPublisher);

        Book book = Book.builder().id(1L).title("Cien años de soledad")
                .status(BookStatus.PRESTADO).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        AppUser borrower = AppUser.builder().id(2L).email("x@test.com").build();

        assertThatThrownBy(() -> loanService.createLoan(1L, borrower))
                .isInstanceOf(BookNotAvailableException.class);

        verify(loanRepository, never()).save(any());
    }

    @Test
    void createsLoanWhenBookIsAvailable() {
        loanService = new LoanService(loanRepository, bookRepository, appUserRepository, reservationRepository, eventPublisher);

        Book book = Book.builder().id(1L).title("Cien años de soledad")
                .status(BookStatus.DISPONIBLE).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        AppUser borrower = AppUser.builder().id(2L).email("x@test.com").build();

        Loan loan = loanService.createLoan(1L, borrower);

        assertThat(loan.getDueDate()).isEqualTo(loan.getLoanDate().plusDays(14));
        assertThat(book.getStatus()).isEqualTo(BookStatus.PRESTADO);
    }

    @Test
    void throwsWhenUserIsBlocked() {
        loanService = new LoanService(loanRepository, bookRepository, appUserRepository, reservationRepository, eventPublisher);

        AppUser blockedUser = AppUser.builder()
                .id(3L)
                .email("bloqueado@test.com")
                .blockedUntil(java.time.LocalDateTime.now().plusDays(3))
                .build();

        assertThatThrownBy(() -> loanService.createLoan(1L, blockedUser))
                .isInstanceOf(UserBlockedException.class);

        verifyNoInteractions(bookRepository);
    }



    @Test
    void thirdLateReturnInWindowBlocksTheAccount() {
        loanService = new LoanService(loanRepository, bookRepository, appUserRepository, reservationRepository, eventPublisher);

        AppUser borrower = AppUser.builder().id(2L).build();
        Book book = Book.builder().id(1L).status(BookStatus.PRESTADO).build();
        Loan loan = Loan.builder().id(10L).book(book).borrower(borrower)
                .loanDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(5)) // vencido
                .build();

        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanRepository.countLateReturnsSince(eq(borrower), any(LocalDate.class))).thenReturn(3L);
        when(reservationRepository.findFirstByBookAndStatusOrderByRequestDateAsc(book, ReservationStatus.PENDIENTE))
                .thenReturn(Optional.empty());

        loanService.returnLoan(10L);

        assertThat(borrower.getBlockedUntil()).isAfter(LocalDateTime.now());
        verify(appUserRepository).save(borrower);
    }

    @Test
    void secondLateReturnDoesNotBlockYet() {
        loanService = new LoanService(loanRepository, bookRepository, appUserRepository, reservationRepository, eventPublisher);

        AppUser borrower = AppUser.builder().id(2L).build();
        Book book = Book.builder().id(1L).status(BookStatus.PRESTADO).build();
        Loan loan = Loan.builder().id(10L).book(book).borrower(borrower)
                .loanDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(5))
                .build();

        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanRepository.countLateReturnsSince(eq(borrower), any(LocalDate.class))).thenReturn(2L);
        when(reservationRepository.findFirstByBookAndStatusOrderByRequestDateAsc(book, ReservationStatus.PENDIENTE))
                .thenReturn(Optional.empty());

        loanService.returnLoan(10L);

        assertThat(borrower.getBlockedUntil()).isNull();
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void returnWithWaitingReservationSetsBookToReservado() {
        loanService = new LoanService(loanRepository, bookRepository, appUserRepository, reservationRepository, eventPublisher);

        AppUser borrower = AppUser.builder().id(2L).build();
        Book book = Book.builder().id(1L).status(BookStatus.PRESTADO).build();
        Loan loan = Loan.builder().id(10L).book(book).borrower(borrower)
                .loanDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(9))
                .build();

        Reservation reservation = Reservation.builder().id(99L).book(book)
                .status(ReservationStatus.PENDIENTE).build();

        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(reservationRepository.findFirstByBookAndStatusOrderByRequestDateAsc(book, ReservationStatus.PENDIENTE))
                .thenReturn(Optional.of(reservation));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        loanService.returnLoan(10L);

        assertThat(book.getStatus()).isEqualTo(BookStatus.RESERVADO);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.NOTIFICADO);
    }

}