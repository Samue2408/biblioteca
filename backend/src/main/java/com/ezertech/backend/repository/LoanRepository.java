package com.ezertech.backend.repository;

import com.ezertech.backend.entity.AppUser;
import com.ezertech.backend.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("""
        SELECT l FROM Loan l
        JOIN FETCH l.book
        JOIN FETCH l.borrower
        WHERE l.borrower = :borrower
        ORDER BY l.loanDate DESC, l.id DESC
        """)
    List<Loan> findByBorrowerWithBookAndBorrower(@Param("borrower") AppUser borrower);

    long countByReturnDateIsNull();

    long countByReturnDateIsNullAndDueDateBefore(LocalDate date);

    List<Loan> findByReturnDateIsNullAndReminderSentAtIsNullAndDueDateBetween(
            LocalDate from, LocalDate to);

    List<Loan> findByReturnDateIsNullAndDueDateBeforeAndOverdueNoticeSentAtIsNull(LocalDate date);

    @Query("""
        SELECT l FROM Loan l
        JOIN FETCH l.book
        JOIN FETCH l.borrower
        WHERE l.id = :id
        """)
    Optional<Loan> findWithBookAndBorrowerById(@Param("id") Long id);

    @Query("""
            SELECT COUNT(l) FROM Loan l
            WHERE l.borrower = :borrower
              AND l.returnDate IS NOT NULL
              AND l.returnDate > l.dueDate
              AND l.returnDate >= :since
            """)
    long countLateReturnsSince(@Param("borrower") AppUser borrower, @Param("since") LocalDate since);


}
