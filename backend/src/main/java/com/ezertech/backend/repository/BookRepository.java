package com.ezertech.backend.repository;


import com.ezertech.backend.entity.Book;
import com.ezertech.backend.entity.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);
    List<Book> findByStatus(BookStatus status);
    long countByStatus(BookStatus status);
}
