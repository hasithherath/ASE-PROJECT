package com.lms.service;

import com.lms.entity.BorrowTransaction;
import java.util.List;

public interface BorrowService {
    BorrowTransaction borrowBook(Long memberId, Long bookId);
    BorrowTransaction returnBook(Long memberId, Long bookId);
    List<BorrowTransaction> getMemberHistory(Long memberId);
    Double calculateTotalFines(Long memberId);
}
