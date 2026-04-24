package com.lms.repository;

import com.lms.entity.BorrowTransaction;
import com.lms.entity.Member;
import com.lms.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {
    List<BorrowTransaction> findByMember(Member member);
    List<BorrowTransaction> findByMemberAndReturnDateIsNull(Member member);
    Optional<BorrowTransaction> findByBookAndMemberAndReturnDateIsNull(Book book, Member member);
    Long countByMemberAndReturnDateIsNull(Member member);
}
