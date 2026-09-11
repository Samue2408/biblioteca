package com.ezertech.backend.service;

import com.ezertech.backend.client.openlibrary.OpenLibraryBookData;
import com.ezertech.backend.client.openlibrary.OpenLibraryClient;
import com.ezertech.backend.dto.book.CreateBookRequest;
import com.ezertech.backend.entity.Book;
import com.ezertech.backend.entity.BookStatus;
import com.ezertech.backend.exception.*;
import com.ezertech.backend.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
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
        String coverUrl = request.coverUrl();

//        boolean needsAutofill = isBlank(title) || isBlank(author);
//
//        if (needsAutofill) {
            Optional<OpenLibraryBookData> data = safeLookup(request.isbn());

            if (data.isPresent()) {
                OpenLibraryBookData d = data.get();
                if (isBlank(title)) title = d.title();
                if (isBlank(author)) author = firstAuthorName(d);
                if (year == null) year = extractYear(d.publishDate());
                if (isBlank(coverUrl) && d.cover() != null) coverUrl = d.cover().large();
            }
//        }

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

    public List<Book> findBooks(String title, String author, BookStatus status) {
        Specification<Book> specification = Specification.unrestricted();

        if (!isBlank(title)) {
            specification = specification.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("title")), containsIgnoreCase(title)));
        }
        if (!isBlank(author)) {
            specification = specification.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("author")), containsIgnoreCase(author)));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("status"), status));
        } else {
            specification = specification.and((root, query, builder) ->
                    builder.notEqual(root.get("status"), BookStatus.ELIMINADO));
        }

        return bookRepository.findAll(specification);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new com.ezertech.backend.exception.ResourceNotFoundException("Libro no encontrado"));

        if (book.getStatus() != BookStatus.DISPONIBLE) {
            throw new BookNotDeletableException("Solo se pueden eliminar libros con status DISPONIBLE");
        }

        book.setStatus(BookStatus.ELIMINADO);
        bookRepository.save(book);
    }

    @Transactional
    public void restoreBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        if (book.getStatus() != BookStatus.ELIMINADO) {
            throw new BookNotRestorableException("El libro no está eliminado y no puede restaurarse");
        }

        book.setStatus(BookStatus.DISPONIBLE);
        bookRepository.save(book);
    }

    // Para que un fallo de Open Library no dañe el registro del libro.
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

    private String containsIgnoreCase(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
