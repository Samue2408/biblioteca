package com.ezertech.backend.event;

// EVENTO PARA PODERLO ESCUCHAR Y MANDAR CORREO DE CONFIRMACION
public record LoanCreatedEvent(Long loanId) {
}
