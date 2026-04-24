package com.lms.service;

import com.lms.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    Page<Book> getAllBooks(Pageable pageable);
    Page<Book> searchBooks(String title, String isbn, String category, Pageable pageable);
    Book addBook(Book book);
    Book updateBook(Long id, Book book);
    void deleteBook(Long id);
    Book getBookById(Long id);
}
