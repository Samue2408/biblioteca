package com.ezertech.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrower", nullable = false)
    private AppUser borrower;

    @NotNull
    @Column(nullable = false)
    private LocalDate loanDate;

    // Regla de negocio: loanDate + 14 dias, se calcula en el servicio al crear el prestamo.
    @NotNull
    @Column(nullable = false)
    private LocalDate dueDate;

    // Nulo mientras el libro no se devuelve.
    private LocalDate returnDate;

    // Evita reenviar el recordatorio de vencimiento proximo mas de una vez.
    private LocalDateTime reminderSentAt;

    // Evita repetir el aviso de vencido.
    private LocalDateTime overdueNoticeSentAt;

    public boolean isOverdue() {
        return returnDate == null && dueDate != null && LocalDate.now().isAfter(dueDate);
    }
}
