package com.ezertech.backend.controller;

import com.ezertech.backend.client.openlibrary.OpenLibraryClient;
import com.ezertech.backend.dto.book.BookLookupResponse;
import com.ezertech.backend.dto.book.BookResponse;
import com.ezertech.backend.dto.book.CreateBookRequest;
import com.ezertech.backend.entity.Book;
import com.ezertech.backend.exception.ResourceNotFoundException;
import com.ezertech.backend.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;
    private final OpenLibraryClient openLibraryClient;

    public BookController(BookService bookService, OpenLibraryClient openLibraryClient) {
        this.bookService = bookService;
        this.openLibraryClient = openLibraryClient;
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody CreateBookRequest request) {
        Book book = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(book));
    }

    @GetMapping("/lookup/{isbn}")
    public ResponseEntity<BookLookupResponse> lookup(@PathVariable String isbn) {
        var data = openLibraryClient.lookupByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró información en Open Library para ISBN " + isbn));

        String author = (data.authors() == null || data.authors().isEmpty())
                ? null : data.authors().get(0).name();
        String cover = data.cover() != null ? data.cover().large() : null;

        return ResponseEntity.ok(new BookLookupResponse(data.title(), author, null, cover));
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
                book.getPublicationYear(), book.getStatus().name(), book.getCoverUrl());
    }
}
