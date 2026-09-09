package com.ezertech.backend.service;

import com.ezertech.backend.entity.AppUser;
import com.ezertech.backend.entity.Book;
import com.ezertech.backend.entity.Reservation;
import com.ezertech.backend.entity.ReservationStatus;
import com.ezertech.backend.exception.DuplicateReservationException;
import com.ezertech.backend.exception.ResourceNotFoundException;
import com.ezertech.backend.repository.BookRepository;
import com.ezertech.backend.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;

    public ReservationService(ReservationRepository reservationRepository, BookRepository bookRepository) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public ReservationCreationResult createReservation(Long bookId, AppUser borrower) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        boolean alreadyWaiting = reservationRepository
                .existsByBookAndBorrowerAndStatus(book, borrower, ReservationStatus.PENDIENTE);

        if (alreadyWaiting) {
            throw new DuplicateReservationException("Ya tienes una reserva activa para este libro");
        }

        long position = reservationRepository.countByBookAndStatus(book, ReservationStatus.PENDIENTE) + 1;

        Reservation reservation = Reservation.builder()
                .book(book)
                .borrower(borrower)
                .requestDate(LocalDateTime.now())
                .status(ReservationStatus.PENDIENTE)
                .build();

        reservationRepository.save(reservation);

        return new ReservationCreationResult(reservation, (int) position);
    }

    public record ReservationCreationResult(Reservation reservation, int position) {
    }
}
