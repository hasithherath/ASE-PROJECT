package com.lms.repository;

import com.lms.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Book> findByIsbn(String isbn, Pageable pageable);
    java.util.Optional<Book> findByIsbn(String isbn);
    Page<Book> findByCategoryContainingIgnoreCase(String category, Pageable pageable);
}
