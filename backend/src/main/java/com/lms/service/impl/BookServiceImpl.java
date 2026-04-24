package com.lms.service.impl;

import com.lms.entity.Book;
import com.lms.exception.BookNotFoundException;
import com.lms.repository.BookRepository;
import com.lms.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Override
    public Page<Book> searchBooks(String title, String isbn, String category, Pageable pageable) {
        if (isbn != null && !isbn.isEmpty()) {
            return bookRepository.findByIsbn(isbn, pageable);
        } else if (title != null && !title.isEmpty()) {
            return bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (category != null && !category.isEmpty()) {
            return bookRepository.findByCategoryContainingIgnoreCase(category, pageable);
        }
        return bookRepository.findAll(pageable);
    }

    @Override
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(Long id, Book book) {
        Book existing = getBookById(id);
        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setIsbn(book.getIsbn());
        existing.setCategory(book.getCategory());
        existing.setAvailableCopies(book.getAvailableCopies());
        return bookRepository.save(existing);
    }

    @Override
    public void deleteBook(Long id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }
}
