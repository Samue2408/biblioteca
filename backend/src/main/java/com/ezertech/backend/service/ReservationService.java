package com.ezertech.backend.service;

import com.ezertech.backend.dto.reservation.ReservationResponse;
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
import java.util.List;

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

    @Transactional
    public void deleteReservation(Long id, AppUser borrower) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (!reservation.getBorrower().getId().equals(borrower.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No puedes eliminar una reserva de otro usuario");
        }

        reservation.setStatus(ReservationStatus.CANCELADO);

        reservationRepository.save(reservation);
    }

    public List<ReservationResponse> getMyReservations(AppUser borrower) {
        List<Reservation> reservations = reservationRepository
                .findWithBookByBorrowerOrderByRequestDateDesc(borrower);

        return reservations.stream()
                .map(this::toResponse)
                .toList();
    }

    private ReservationResponse toResponse(Reservation reservation) {
        int position = 0;

        if (reservation.getStatus() == ReservationStatus.PENDIENTE) {
            long ahead = reservationRepository.countByBookAndStatusAndRequestDateBefore(
                    reservation.getBook(), ReservationStatus.PENDIENTE, reservation.getRequestDate());
            position = (int) ahead + 1;
        }

        return new ReservationResponse(
                reservation.getId(),
                reservation.getBook().getTitle(),
                position,
                reservation.getStatus().name()
        );
    }

    public record ReservationCreationResult(Reservation reservation, int position) {
    }
}
