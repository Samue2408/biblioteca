package com.ezertech.backend.dto.book;

import jakarta.validation.constraints.NotBlank;

public record CreateBookRequest(
        @NotBlank String isbn,
        String title,
        String author,
        Integer publicationYear,
        String coverUrl
) {
}
