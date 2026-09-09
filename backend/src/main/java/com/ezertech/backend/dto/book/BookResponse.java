package com.ezertech.backend.dto.book;

public record BookResponse(
        Long id, String title, String author, String isbn,
        Integer publicationYear, String status, String coverUrl
) {
}
