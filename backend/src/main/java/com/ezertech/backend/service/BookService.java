package com.ezertech.backend.service;

import com.ezertech.backend.client.openlibrary.OpenLibraryBookData;
import com.ezertech.backend.client.openlibrary.OpenLibraryClient;
import com.ezertech.backend.dto.book.CreateBookRequest;
import com.ezertech.backend.entity.Book;
import com.ezertech.backend.entity.BookStatus;
import com.ezertech.backend.exception.DuplicateIsbnException;
import com.ezertech.backend.exception.ExternalBookLookupException;
import com.ezertech.backend.exception.MissingBookDataException;
import com.ezertech.backend.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BookService {
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");

    private final BookRepository bookRepository;
    private final OpenLibraryClient openLibraryClient;

    public BookService(BookRepository bookRepository, OpenLibraryClient openLibraryClient) {
        this.bookRepository = bookRepository;
        this.openLibraryClient = openLibraryClient;
    }

    @Transactional
    public Book createBook(CreateBookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateIsbnException("Ya existe un libro con ISBN " + request.isbn());
        }

        String title = request.title();
        String author = request.author();
        Integer year = request.publicationYear();
        String coverUrl = null;

        boolean needsAutofill = isBlank(title) || isBlank(author);

        if (needsAutofill) {
            Optional<OpenLibraryBookData> data = safeLookup(request.isbn());

            if (data.isPresent()) {
                OpenLibraryBookData d = data.get();
                if (isBlank(title)) title = d.title();
                if (isBlank(author)) author = firstAuthorName(d);
                if (year == null) year = extractYear(d.publishDate());
                if (d.cover() != null) coverUrl = d.cover().large();
            }
        }

        if (isBlank(title) || isBlank(author)) {
            throw new MissingBookDataException(
                    "No se encontró título/autor en Open Library; indícalos manualmente");
        }

        Book book = Book.builder()
                .title(title)
                .author(author)
                .isbn(request.isbn())
                .publicationYear(year)
                .coverUrl(coverUrl)
                .status(BookStatus.DISPONIBLE)
                .build();

        return bookRepository.save(book);
    }

    // Nunca deja que un fallo de Open Library reviente el registro del libro.
    private Optional<OpenLibraryBookData> safeLookup(String isbn) {
        try {
            return openLibraryClient.lookupByIsbn(isbn);
        } catch (ExternalBookLookupException e) {
            return Optional.empty();
        }
    }

    private String firstAuthorName(OpenLibraryBookData data) {
        if (data.authors() == null || data.authors().isEmpty()) {
            return null;
        }
        return data.authors().get(0).name();
    }

    private Integer extractYear(String publishDate) {
        if (publishDate == null) return null;
        Matcher matcher = YEAR_PATTERN.matcher(publishDate);
        return matcher.find() ? Integer.parseInt(matcher.group()) : null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
