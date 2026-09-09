package com.ezertech.backend.dto.reservation;

public record ReservationResponse(Long id, String bookTitle, int position, String status) {
}