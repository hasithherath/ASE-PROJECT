package com.lms.service.impl;

import com.lms.entity.Book;
import com.lms.entity.BorrowTransaction;
import com.lms.entity.Member;
import com.lms.exception.BookNotFoundException;
import com.lms.exception.IneligibleMemberException;
import com.lms.repository.BookRepository;
import com.lms.repository.BorrowTransactionRepository;
import com.lms.repository.MemberRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ============================================================
 * BorrowServiceImplMockTest.java
 * SE3112 — Advanced Software Engineering
 * Student 2 — JUnit Feature: Mocking & Exception Testing
 * ============================================================
 *
 * Tests BorrowServiceImpl and BookServiceImpl in complete
 * isolation using Mockito — no Spring context, no database.
 *
 * Key Concepts Demonstrated:
 * 
 * @Mock — Creates fake repository objects
 * @InjectMocks — Injects mocks into the real service
 * @ExtendWith — Activates Mockito for JUnit 5
 *             when().thenReturn()— Stubs what the fake repo returns
 *             when().thenThrow() — Simulates database failures
 *             assertThrows() — Verifies correct exceptions are raised
 *             verify() — Confirms save() is called the right
 *             number of times
 *             ============================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Student 2 — Mocking & Exception Testing")
class BorrowServiceImplMockTest {

    // ── Mocks: fake versions of the database repositories ───────────────────
    @Mock
    private BorrowTransactionRepository transactionRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    // ── InjectMocks: the REAL service, but with fake repos injected ──────────
    @InjectMocks
    private BorrowServiceImpl borrowService;

    // ── Test fixtures: reusable objects rebuilt before every test ────────────
    private Member sampleMember;
    private Book availableBook;
    private Book unavailableBook;

    @BeforeEach
    void setUp() {
        // Fresh objects before every single test to avoid state leaking between tests
        sampleMember = new Member();
        sampleMember.setId(1L);

        availableBook = new Book();
        availableBook.setId(101L);
        availableBook.setTitle("Clean Code");
        availableBook.setAvailableCopies(3); // has copies → can be borrowed

        unavailableBook = new Book();
        unavailableBook.setId(102L);
        unavailableBook.setTitle("Refactoring");
        unavailableBook.setAvailableCopies(0); // no copies → cannot be borrowed
    }

    // =========================================================================
    // SECTION A — borrowBook() — Happy Path
    // =========================================================================

    @Test
    @DisplayName("A1: borrowBook() — succeeds when member and book both exist and book is available")
    void borrowBook_WhenValidMemberAndAvailableBook_ReturnsTransaction() {
        // ARRANGE
        // Stub memberRepository: when findById(1) is called → return our sample member
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        // Stub bookRepository: when findById(101) is called → return our available book
        when(bookRepository.findById(101L)).thenReturn(Optional.of(availableBook));
        // Stub: member currently has 0 active borrows → well below MAX_BOOKS limit
        when(transactionRepository.countByMemberAndReturnDateIsNull(sampleMember)).thenReturn(0L);
        // Stub: transactionRepository.save() returns back whatever transaction is
        // passed in
        when(transactionRepository.save(any(BorrowTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        BorrowTransaction result = borrowService.borrowBook(1L, 101L);

        // ASSERT
        assertNotNull(result, "Transaction should not be null on successful borrow");
        assertEquals(sampleMember, result.getMember(), "Transaction must reference the correct member");
        assertEquals(availableBook, result.getBook(), "Transaction must reference the correct book");
        assertNotNull(result.getBorrowDate(), "Borrow date must be set");
        assertNotNull(result.getDueDate(), "Due date must be set");
        assertEquals(LocalDate.now().plusDays(14), result.getDueDate(),
                "Due date should be exactly 14 days from today");

        // VERIFY
        verify(bookRepository, times(1)).save(availableBook); // book copies updated
        verify(transactionRepository, times(1)).save(any(BorrowTransaction.class)); // record saved
    }

    // =========================================================================
    // SECTION B — borrowBook() — Member Not Found
    // =========================================================================

    @Test
    @DisplayName("B1: borrowBook() — throws RuntimeException when member ID does not exist")
    void borrowBook_WhenMemberNotFound_ThrowsRuntimeException() {
        // ARRANGE — member 999 does not exist in the database
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT — assertThrows captures the exception so we can inspect it
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> borrowService.borrowBook(999L, 101L),
                "Should throw RuntimeException when member is not found");

        assertTrue(exception.getMessage().contains("Member not found"),
                "Exception message should say 'Member not found'");

        // VERIFY — book repo and transaction repo must never be touched
        verify(bookRepository, never()).findById(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("B2: borrowBook() — throws RuntimeException when memberRepository itself throws")
    void borrowBook_WhenMemberRepositoryThrowsRuntimeException_Propagates() {
        // ARRANGE — simulate database connection failure
        when(memberRepository.findById(anyLong()))
                .thenThrow(new RuntimeException("Database connection lost"));

        // ACT & ASSERT
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> borrowService.borrowBook(1L, 101L));

        assertEquals("Database connection lost", ex.getMessage());

        // VERIFY — save() was never reached
        verify(transactionRepository, never()).save(any());
        verify(bookRepository, never()).save(any());
    }

    // =========================================================================
    // SECTION C — borrowBook() — Book Not Found
    // =========================================================================

    @Test
    @DisplayName("C1: borrowBook() — throws RuntimeException when book ID does not exist")
    void borrowBook_WhenBookNotFound_ThrowsRuntimeException() {
        // ARRANGE — member exists, but book 999 does not
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> borrowService.borrowBook(1L, 999L),
                "Should throw RuntimeException when book is not found");

        assertTrue(exception.getMessage().contains("Book not found"),
                "Exception message should say 'Book not found'");

        // VERIFY — transaction save() must never be called when book doesn't exist
        verify(transactionRepository, never()).save(any());
        verify(bookRepository, never()).save(any());
    }

    // =========================================================================
    // SECTION D — borrowBook() — Book Not Available (0 copies)
    // =========================================================================

    @Test
    @DisplayName("D1: borrowBook() — throws RuntimeException when book has 0 available copies")
    void borrowBook_WhenBookHasNoCopies_ThrowsRuntimeException() {
        // ARRANGE — book exists but availableCopies = 0
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findById(102L)).thenReturn(Optional.of(unavailableBook));
        when(transactionRepository.countByMemberAndReturnDateIsNull(sampleMember)).thenReturn(0L);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> borrowService.borrowBook(1L, 102L),
                "Should throw RuntimeException when book has no available copies");

        assertTrue(exception.getMessage().contains("unavailable"),
                "Exception message should mention 'unavailable'");

        // VERIFY — save() must NEVER be called when book has no copies
        verify(bookRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    // =========================================================================
    // SECTION E — borrowBook() — Member Exceeds Borrow Limit
    // =========================================================================

    @Test
    @DisplayName("E1: borrowBook() — throws IneligibleMemberException when member already has 5 books")
    void borrowBook_WhenMemberAtBorrowLimit_ThrowsIneligibleMemberException() {
        // ARRANGE — member already has 5 active borrows (MAX_BOOKS = 5)
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findById(101L)).thenReturn(Optional.of(availableBook));
        when(transactionRepository.countByMemberAndReturnDateIsNull(sampleMember)).thenReturn(5L);

        // ACT & ASSERT
        IneligibleMemberException exception = assertThrows(
                IneligibleMemberException.class,
                () -> borrowService.borrowBook(1L, 101L),
                "Should throw IneligibleMemberException when member is at the 5-book limit");

        assertTrue(exception.getMessage().contains("5"),
                "Exception message should mention the limit of 5 books");

        // VERIFY — save() must NOT be called when member is ineligible
        verify(bookRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("E2: borrowBook() — throws IneligibleMemberException when member has MORE than 5 books")
    void borrowBook_WhenMemberExceedsBorrowLimit_ThrowsIneligibleMemberException() {
        // ARRANGE — member somehow has 7 active borrows (edge case)
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findById(101L)).thenReturn(Optional.of(availableBook));
        when(transactionRepository.countByMemberAndReturnDateIsNull(sampleMember)).thenReturn(7L);

        // ACT & ASSERT
        assertThrows(IneligibleMemberException.class,
                () -> borrowService.borrowBook(1L, 101L),
                "Should still throw IneligibleMemberException when borrows exceed 5");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("E3: borrowBook() — succeeds when member has exactly 4 books (one below limit)")
    void borrowBook_WhenMemberHasFourBooks_Succeeds() {
        // ARRANGE — member has 4 borrows → still eligible for one more
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findById(101L)).thenReturn(Optional.of(availableBook));
        when(transactionRepository.countByMemberAndReturnDateIsNull(sampleMember)).thenReturn(4L);
        when(bookRepository.save(any())).thenReturn(availableBook);
        when(transactionRepository.save(any(BorrowTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // ACT — should NOT throw
        BorrowTransaction result = assertDoesNotThrow(
                () -> borrowService.borrowBook(1L, 101L),
                "Member with 4 borrows should still be eligible");

        assertNotNull(result);
    }

    // =========================================================================
    // SECTION F — returnBook() — Exception Testing
    // =========================================================================

    @Test
    @DisplayName("F1: returnBook() — throws RuntimeException when no active transaction exists")
    void returnBook_WhenNoActiveTransaction_ThrowsRuntimeException() {
        // ARRANGE — member and book exist, but there's no active borrow record for them
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findById(101L)).thenReturn(Optional.of(availableBook));
        when(transactionRepository.findByBookAndMemberAndReturnDateIsNull(availableBook, sampleMember))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> borrowService.returnBook(1L, 101L),
                "Should throw RuntimeException when there is no active borrow transaction");

        assertTrue(exception.getMessage().contains("No active transaction"),
                "Exception message should mention 'No active transaction'");

        // VERIFY — save() must never be called when no transaction is found
        verify(transactionRepository, never()).save(any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("F2: returnBook() — throws RuntimeException when member not found during return")
    void returnBook_WhenMemberNotFound_ThrowsRuntimeException() {
        // ARRANGE — member 999 does not exist
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> borrowService.returnBook(999L, 101L));

        assertTrue(ex.getMessage().contains("Member not found"));
        verify(bookRepository, never()).findById(any());
    }

    // =========================================================================
    // SECTION G — getMemberHistory() — Exception Testing
    // =========================================================================

    @Test
    @DisplayName("G1: getMemberHistory() — throws RuntimeException when member does not exist")
    void getMemberHistory_WhenMemberNotFound_ThrowsRuntimeException() {
        // ARRANGE
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> borrowService.getMemberHistory(999L),
                "Should throw when member not found in history request");

        assertTrue(ex.getMessage().contains("Member not found"));
        verify(transactionRepository, never()).findByMember(any());
    }

    @Test
    @DisplayName("G2: getMemberHistory() — returns empty list when member has no borrow history")
    void getMemberHistory_WhenMemberHasNoHistory_ReturnsEmptyList() {
        // ARRANGE
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(transactionRepository.findByMember(sampleMember)).thenReturn(List.of());

        // ACT
        List<BorrowTransaction> history = borrowService.getMemberHistory(1L);

        // ASSERT
        assertNotNull(history);
        assertTrue(history.isEmpty(), "History should be empty for a member with no borrows");
        verify(transactionRepository, times(1)).findByMember(sampleMember);
    }

    // =========================================================================
    // SECTION H — calculateTotalFines() — Exception Testing
    // =========================================================================

    @Test
    @DisplayName("H1: calculateTotalFines() — throws RuntimeException when member does not exist")
    void calculateTotalFines_WhenMemberNotFound_ThrowsRuntimeException() {
        // ARRANGE
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> borrowService.calculateTotalFines(999L));

        assertTrue(ex.getMessage().contains("Member not found"));
    }

    @Test
    @DisplayName("H2: calculateTotalFines() — returns 0.0 when member has no overdue fines")
    void calculateTotalFines_WhenNoFines_ReturnsZero() {
        // ARRANGE — create 2 transactions both with fineAmount = 0
        BorrowTransaction t1 = new BorrowTransaction();
        t1.setFineAmount(0.0);
        BorrowTransaction t2 = new BorrowTransaction();
        t2.setFineAmount(0.0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(transactionRepository.findByMember(sampleMember)).thenReturn(List.of(t1, t2));

        // ACT
        Double total = borrowService.calculateTotalFines(1L);

        // ASSERT
        assertEquals(0.0, total, 0.001, "Total fines should be 0.0 when no overdue books");
    }

    @Test
    @DisplayName("H3: calculateTotalFines() — correctly sums fines across multiple transactions")
    void calculateTotalFines_WithMultipleOverdueBooks_ReturnsSumOfFines() {
        // ARRANGE — 3 transactions with different fine amounts
        BorrowTransaction t1 = new BorrowTransaction();
        t1.setFineAmount(5.0); // 5 days overdue

        BorrowTransaction t2 = new BorrowTransaction();
        t2.setFineAmount(12.0); // 12 days overdue

        BorrowTransaction t3 = new BorrowTransaction();
        t3.setFineAmount(0.0); // returned on time

        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(transactionRepository.findByMember(sampleMember)).thenReturn(List.of(t1, t2, t3));

        // ACT
        Double total = borrowService.calculateTotalFines(1L);

        // ASSERT
        assertEquals(17.0, total, 0.001, "Total fines should be 5 + 12 + 0 = 17.0");
    }

    // =========================================================================
    // SECTION I — verify() Interaction Counts (Advanced Mockito)
    // =========================================================================

    @Test
    @DisplayName("I1: verify() — borrowBook() calls bookRepository.save() exactly once on success")
    void borrowBook_OnSuccess_CallsBookSaveExactlyOnce() {
        // ARRANGE
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findById(101L)).thenReturn(Optional.of(availableBook));
        when(transactionRepository.countByMemberAndReturnDateIsNull(sampleMember)).thenReturn(0L);
        when(bookRepository.save(any())).thenReturn(availableBook);
        when(transactionRepository.save(any(BorrowTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // ACT
        borrowService.borrowBook(1L, 101L);

        // VERIFY — book saved exactly once (to decrement availableCopies)
        verify(bookRepository, times(1)).save(availableBook);
        // transaction saved exactly once
        verify(transactionRepository, times(1)).save(any(BorrowTransaction.class));
    }

    @Test
    @DisplayName("I2: verify() — borrowBook() calls never save() when book not found")
    void borrowBook_WhenBookNotFound_NeverCallsSave() {
        // ARRANGE
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT — expect exception
        assertThrows(RuntimeException.class, () -> borrowService.borrowBook(1L, 999L));

        // VERIFY — absolutely no save() calls made
        verify(bookRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("I3: verify() — borrowBook() never calls save() when member is ineligible")
    void borrowBook_WhenMemberIneligible_NeverCallsSave() {
        // ARRANGE
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findById(101L)).thenReturn(Optional.of(availableBook));
        when(transactionRepository.countByMemberAndReturnDateIsNull(sampleMember)).thenReturn(5L);

        // ACT — expect exception
        assertThrows(IneligibleMemberException.class, () -> borrowService.borrowBook(1L, 101L));

        // VERIFY
        verify(bookRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("I4: verify() — getMemberHistory() queries transactionRepository exactly once")
    void getMemberHistory_QueriesTransactionRepositoryExactlyOnce() {
        // ARRANGE
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(transactionRepository.findByMember(sampleMember)).thenReturn(List.of());

        // ACT
        borrowService.getMemberHistory(1L);

        // VERIFY — only one database query, no unexpected calls
        verify(transactionRepository, times(1)).findByMember(sampleMember);
        verifyNoMoreInteractions(transactionRepository);
    }
}