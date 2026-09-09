package com.ezertech.backend.repository;

import com.ezertech.backend.entity.AppUser;
import com.ezertech.backend.entity.Book;
import com.ezertech.backend.entity.Reservation;
import com.ezertech.backend.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findFirstByBookAndStatusOrderByRequestDateAsc(
            Book book, ReservationStatus status);

    List<Reservation> findByBorrower(AppUser borrower);

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.book
        JOIN FETCH r.borrower
        WHERE r.id = :id
        """)
    Optional<Reservation> findWithBookAndBorrowerById(@Param("id") Long id);

    long countByBookAndStatus(Book book, ReservationStatus status);

    boolean existsByBookAndBorrowerAndStatus(Book book, AppUser borrower, ReservationStatus status);

}
