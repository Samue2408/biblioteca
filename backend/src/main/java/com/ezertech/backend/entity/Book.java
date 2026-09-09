package com.ezertech.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String author;

    // Unico. Se valida tambien en el servicio para lanzar DuplicateIsbnException
    // con un mensaje controlado en vez de dejar que reviente la constraint de BD.
    @NotBlank
    @Column(nullable = false, unique = true)
    private String isbn;

    private Integer publicationYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookStatus status = BookStatus.DISPONIBLE;

    // Datos que se completan al autocompletar por ISBN contra Open Library.
    private String coverUrl;

    private String subjects; // TODO: valorar ElementCollection si se necesita lista real
}
