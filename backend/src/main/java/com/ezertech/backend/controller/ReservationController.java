package com.ezertech.backend.controller;

import com.ezertech.backend.dto.reservation.CreateReservationRequest;
import com.ezertech.backend.dto.reservation.ReservationResponse;
import com.ezertech.backend.entity.AppUser;
import com.ezertech.backend.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request,
                                                      @AuthenticationPrincipal AppUser borrower) {
        var result = reservationService.createReservation(request.bookId(), borrower);

        ReservationResponse response = new ReservationResponse(
                result.reservation().getId(),
                result.reservation().getBook().getTitle(),
                result.position(),
                result.reservation().getStatus().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser borrower) {

        reservationService.deleteReservation(id, borrower);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationResponse>> mine(@AuthenticationPrincipal AppUser borrower) {
        return ResponseEntity.ok(reservationService.getMyReservations(borrower));
    }
}
