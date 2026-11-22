package com.example.bookservice.repo;

import java.util.Optional;

import com.example.bookservice.domain.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByTitle(String title);

    /**
     * Verrou pessimiste cote base : les autres transactions attendent.
     * C'est ce qui rend l'emprunt correct avec 3 instances (3 JVM).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);
}
