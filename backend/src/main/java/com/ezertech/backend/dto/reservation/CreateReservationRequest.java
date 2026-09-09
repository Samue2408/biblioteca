package com.ezertech.backend.dto.reservation;

import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(@NotNull Long bookId) {
}