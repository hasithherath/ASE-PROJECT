package com.lms.service.impl;

import com.lms.entity.Book;
import com.lms.entity.BorrowTransaction;
import com.lms.entity.Member;
import com.lms.exception.IneligibleMemberException;
import com.lms.repository.BookRepository;
import com.lms.repository.BorrowTransactionRepository;
import com.lms.repository.MemberRepository;
import com.lms.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BorrowServiceImpl implements BorrowService {
    private final BorrowTransactionRepository transactionRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    private static final int MAX_BOOKS = 5;
    private static final double FINE_PER_DAY = 1.0;
    private static final int BORROW_DAYS = 14;

    @Autowired
    public BorrowServiceImpl(BorrowTransactionRepository transactionRepository, 
                             BookRepository bookRepository, 
                             MemberRepository memberRepository) {
        this.transactionRepository = transactionRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public BorrowTransaction borrowBook(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Long currentBorrows = transactionRepository.countByMemberAndReturnDateIsNull(member);
        if (currentBorrows >= MAX_BOOKS) {
            throw new IneligibleMemberException("Member has reached the limit of " + MAX_BOOKS + " books.");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book is currently unavailable.");
        }

        BorrowTransaction transaction = new BorrowTransaction();
        transaction.setMember(member);
        transaction.setBook(book);
        transaction.setBorrowDate(LocalDate.now());
        transaction.setDueDate(LocalDate.now().plusDays(BORROW_DAYS));

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public BorrowTransaction returnBook(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        BorrowTransaction transaction = transactionRepository.findByBookAndMemberAndReturnDateIsNull(book, member)
                .orElseThrow(() -> new RuntimeException("No active transaction found for this book and member"));

        transaction.setReturnDate(LocalDate.now());

        long overdueDays = ChronoUnit.DAYS.between(transaction.getDueDate(), transaction.getReturnDate());
        if (overdueDays > 0) {
            transaction.setFineAmount(overdueDays * FINE_PER_DAY);
        } else {
            transaction.setFineAmount(0.0);
        }

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return transactionRepository.save(transaction);
    }

    @Override
    public List<BorrowTransaction> getMemberHistory(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return transactionRepository.findByMember(member);
    }

    @Override
    public Double calculateTotalFines(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return transactionRepository.findByMember(member).stream()
                .mapToDouble(BorrowTransaction::getFineAmount)
                .sum();
    }
}
