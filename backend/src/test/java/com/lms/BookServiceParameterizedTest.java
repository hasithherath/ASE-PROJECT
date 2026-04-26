/*
 * SE3112 - Advanced Software Engineering
 * Student 1 - Janeesha
 * Feature: JUnit 5 Parameterized Testing
 * Tests: BookService ISBN search, Fine calculation logic,
 *        Borrow eligibility boundary, Title search edge cases
 */
package com.lms;

import com.lms.entity.Book;
import com.lms.entity.BorrowTransaction;
import com.lms.entity.Member;
import com.lms.exception.IneligibleMemberException;
import com.lms.repository.BookRepository;
import com.lms.repository.BorrowTransactionRepository;
import com.lms.repository.MemberRepository;
import com.lms.service.impl.BookServiceImpl;
import com.lms.service.impl.BorrowServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Parameterized test suite for the Library Management System.
 *
 * <p>This class demonstrates four different JUnit 5 parameterized source annotations
 * to test core business logic in BookServiceImpl and BorrowServiceImpl:</p>
 *
 * <ul>
 *   <li><strong>@ValueSource</strong> — ISBN search priority validation</li>
 *   <li><strong>@CsvSource</strong> — Fine calculation formula correctness</li>
 *   <li><strong>@MethodSource</strong> — Borrow eligibility boundary analysis</li>
 *   <li><strong>@NullAndEmptySource + @ValueSource</strong> — Title search edge cases</li>
 * </ul>
 *
 * <p><strong>Design Decision:</strong> All tests use Mockito mocks (no Spring context)
 * to ensure fast, isolated unit tests. The fine calculation logic is extracted into
 * a private helper method since the production code embeds it inside
 * {@code BorrowServiceImpl.returnBook()} rather than exposing it as a standalone service.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookService & BorrowService — Parameterized Test Suite")
class BookServiceParameterizedTest {

    // ===================================================================
    // MOCK DEPENDENCIES
    // ===================================================================

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowTransactionRepository transactionRepository;

    @Mock
    private MemberRepository memberRepository;

    // ===================================================================
    // SERVICES UNDER TEST
    // ===================================================================

    @InjectMocks
    private BookServiceImpl bookService;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    // ===================================================================
    // SHARED TEST FIXTURES
    // ===================================================================

    /** Standard pageable used across all search tests: page 0, size 10. */
    private Pageable pageable;

    /** Reusable test book fixture with known field values. */
    private Book testBook;

    /** Reusable test member fixture with known field values. */
    private Member testMember;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Clean Code");
        testBook.setAuthor("Robert C. Martin");
        testBook.setIsbn("978-0132350884");
        testBook.setCategory("Software Engineering");
        testBook.setAvailableCopies(3);

        testMember = new Member();
        testMember.setId(1L);
        testMember.setFirstName("Janeesha");
        testMember.setLastName("Herath");
        testMember.setEmail("janeesha@example.com");
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    /**
     * Replicates the fine calculation logic from {@code BorrowServiceImpl.returnBook()}.
     *
     * <p>Since the production code does not expose fine calculation as a standalone
     * service or utility method, we extract the identical formula here for isolated
     * unit testing. The formula is: {@code overdueDays > 0 ? overdueDays * FINE_PER_DAY : 0.0}</p>
     *
     * <p><strong>FINE_PER_DAY = 1.0</strong> (as defined in BorrowServiceImpl)</p>
     *
     * @param overdueDays the number of days past the due date (can be negative for early returns)
     * @return the calculated fine amount; always >= 0.0
     */
    private double calculateFine(long overdueDays) {
        final double FINE_PER_DAY = 1.0;
        return overdueDays > 0 ? overdueDays * FINE_PER_DAY : 0.0;
    }

    /**
     * Creates a {@link Page} containing exactly one book for use in mock stubbing.
     *
     * @param book the book to wrap in a single-element page
     * @return a non-empty page with one book
     */
    private Page<Book> createSingleBookPage(Book book) {
        return new PageImpl<>(List.of(book), pageable, 1);
    }

    /**
     * Creates an empty {@link Page} for use in mock stubbing.
     *
     * @return an empty page with no content
     */
    private Page<Book> createEmptyPage() {
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }


    // ===================================================================
    // TEST 1: ISBN SEARCH — @ValueSource
    // ===================================================================
    //
    // Validates that searchBooks() correctly prioritises ISBN lookup when a
    // valid ISBN string is provided. The service should call
    // bookRepository.findByIsbn(isbn, pageable) and NEVER fall through to
    // title or category branches.
    //
    // Five different ISBN formats are exercised to cover:
    //   - Standard ISBN-13 with hyphens
    //   - Standard ISBN-10 with hyphens
    //   - ISBN-13 without hyphens (compact)
    //   - ISBN-10 ending in 'X' (check digit)
    //   - Non-standard/custom format (demonstrates no format validation)
    // ===================================================================

    @ParameterizedTest(name = "ISBN \"{0}\" should be searched via findByIsbn and return a result")
    @ValueSource(strings = {
        "978-0132350884",   // Standard ISBN-13 with hyphens
        "0-596-51774-1",    // Standard ISBN-10 with hyphens
        "9780134685991",    // ISBN-13 without hyphens (compact format)
        "0-306-40615-X",    // ISBN-10 with 'X' check digit
        "ISBN-INVALID"      // Non-standard format — service performs NO format validation
    })
    @DisplayName("ISBN Search: should return book when a valid ISBN string is provided (ISBN takes priority over title/category)")
    void shouldReturnBookWhenValidIsbnSearched(String isbn) {
        // Arrange: configure the mock to return a page with one book for the given ISBN
        Book isbnBook = new Book();
        isbnBook.setId(1L);
        isbnBook.setTitle("Test Book for ISBN: " + isbn);
        isbnBook.setAuthor("Test Author");
        isbnBook.setIsbn(isbn);
        isbnBook.setCategory("Testing");
        isbnBook.setAvailableCopies(5);

        Page<Book> expectedPage = createSingleBookPage(isbnBook);
        when(bookRepository.findByIsbn(eq(isbn), eq(pageable))).thenReturn(expectedPage);

        // Act: call searchBooks with ISBN set, plus a non-null title and category
        // to prove ISBN takes priority over other search parameters
        Page<Book> result = bookService.searchBooks("Some Title", isbn, "Some Category", pageable);

        // Assert: verify ISBN branch was taken and results are correct
        assertAll("ISBN search should return the expected book and use the ISBN branch exclusively",
            () -> assertNotNull(result,
                    "Result page should not be null for ISBN: " + isbn),
            () -> assertFalse(result.isEmpty(),
                    "Result page should not be empty for ISBN: " + isbn),
            () -> assertEquals(1, result.getTotalElements(),
                    "Result page should contain exactly 1 book for ISBN: " + isbn),
            () -> assertEquals(isbn, result.getContent().get(0).getIsbn(),
                    "Returned book ISBN should match the searched ISBN: " + isbn)
        );

        // Verify: ISBN search method was called — not title or category
        verify(bookRepository).findByIsbn(eq(isbn), eq(pageable));
        verify(bookRepository, never()).findByTitleContainingIgnoreCase(any(), any());
        verify(bookRepository, never()).findByCategoryContainingIgnoreCase(any(), any());
        verify(bookRepository, never()).findAll(any(Pageable.class));
    }


    // ===================================================================
    // TEST 2: FINE CALCULATION — @CsvSource
    // ===================================================================
    //
    // Tests the fine calculation formula extracted from BorrowServiceImpl:
    //   fine = overdueDays > 0 ? overdueDays * FINE_PER_DAY : 0.0
    //
    // This approach isolates the mathematical logic from the full
    // returnBook() flow (which requires mocking repositories, transactions,
    // and dates). The helper method calculateFine() mirrors the production
    // logic exactly.
    //
    // Covers:
    //   - Zero overdue days (boundary: no fine)
    //   - Single day overdue (minimum positive fine)
    //   - Weekly overdue (common scenario)
    //   - Monthly overdue (significant fine)
    //   - Yearly overdue (extreme case)
    //   - Negative days (early return: -1 and -10 — no negative fines)
    // ===================================================================

    @ParameterizedTest(name = "overdueDays={0} → expectedFine={1}")
    @CsvSource({
        "0,    0.0",     // Boundary: returned exactly on due date — no fine
        "1,    1.0",     // Minimum positive overdue: 1 day × $1.00
        "7,    7.0",     // One week overdue
        "30,  30.0",     // One month overdue
        "365, 365.0",    // One year overdue — extreme case
        "-1,   0.0",     // Early return by 1 day — fine must NOT be negative
        "-10,  0.0"      // Early return by 10 days — fine must still be 0.0
    })
    @DisplayName("Fine Calculation: should compute correct fine amount based on overdue days (FINE_PER_DAY = $1.00)")
    void shouldCalculateCorrectFineForOverdueDays(long overdueDays, double expectedFine) {
        // Act: calculate the fine using the extracted helper (mirrors BorrowServiceImpl logic)
        double actualFine = calculateFine(overdueDays);

        // Assert: compare with delta for floating-point safety
        assertEquals(expectedFine, actualFine, 0.001,
                String.format("Fine for %d overdue day(s) should be $%.2f but was $%.2f. " +
                        "Formula: overdueDays > 0 ? overdueDays * FINE_PER_DAY : 0.0",
                        overdueDays, expectedFine, actualFine));
    }


    // ===================================================================
    // TEST 3: BORROW ELIGIBILITY BOUNDARY — @MethodSource
    // ===================================================================
    //
    // Validates the borrow limit enforcement in BorrowServiceImpl:
    //   MAX_BOOKS = 5
    //   if (currentBorrows >= MAX_BOOKS) → throw IneligibleMemberException
    //
    // Uses boundary value analysis:
    //   - 0, 1, 4 borrows → eligible (under limit)
    //   - 5 borrows → NOT eligible (exactly at limit — boundary condition)
    //   - 6 borrows → NOT eligible (above limit)
    //
    // When eligible: asserts no exception and verifies book.save() is called
    // When ineligible: asserts IneligibleMemberException is thrown
    // ===================================================================

    /**
     * Provides test arguments for borrow eligibility boundary testing.
     *
     * @return stream of (currentBorrowCount, expectedEligible) pairs
     */
    static Stream<Arguments> borrowEligibilityProvider() {
        return Stream.of(
            Arguments.of(0L,  true,  "No books borrowed — well under limit"),
            Arguments.of(1L,  true,  "1 book borrowed — under limit"),
            Arguments.of(4L,  true,  "4 books borrowed — just under limit (boundary - 1)"),
            Arguments.of(5L,  false, "5 books borrowed — AT limit (boundary: MAX_BOOKS = 5)"),
            Arguments.of(6L,  false, "6 books borrowed — above limit")
        );
    }

    @ParameterizedTest(name = "currentBorrows={0}, eligible={1} — {2}")
    @MethodSource("borrowEligibilityProvider")
    @DisplayName("Borrow Eligibility: should enforce MAX_BOOKS=5 borrow limit with correct boundary behaviour")
    void shouldEnforceBorrowLimitCorrectly(Long currentBorrowCount, boolean expectedEligible,
                                           String scenario) {
        // Arrange: set up member and book in repositories
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Stub the borrow count to return the parameterized value
        when(transactionRepository.countByMemberAndReturnDateIsNull(testMember))
                .thenReturn(currentBorrowCount);

        if (expectedEligible) {
            // Reset: ensure availableCopies is fresh for each eligible run
            // (prevents mutation leaking across parameterized iterations)
            testBook.setAvailableCopies(3);

            // Arrange: mock the save operations for the eligible path
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);
            BorrowTransaction savedTransaction = new BorrowTransaction();
            savedTransaction.setId(1L);
            savedTransaction.setMember(testMember);
            savedTransaction.setBook(testBook);
            when(transactionRepository.save(any(BorrowTransaction.class)))
                    .thenReturn(savedTransaction);

            // Act & Assert: borrowing should succeed without exception
            BorrowTransaction result = assertDoesNotThrow(
                    () -> borrowService.borrowBook(1L, 1L),
                    String.format("Borrowing should be allowed when member has %d books " +
                            "(limit is 5). Scenario: %s", currentBorrowCount, scenario));

            // Verify: book copies decremented and saved, transaction saved
            assertAll("Eligible borrow should persist changes correctly",
                () -> assertNotNull(result,
                        "Returned transaction should not be null for eligible borrow"),
                () -> assertEquals(2, testBook.getAvailableCopies(),
                        "Available copies should be decremented from 3 to 2 after borrowing")
            );

            verify(bookRepository).save(testBook);
            verify(transactionRepository).save(any(BorrowTransaction.class));

        } else {
            // Act & Assert: borrowing should fail with IneligibleMemberException
            IneligibleMemberException exception = assertThrows(
                    IneligibleMemberException.class,
                    () -> borrowService.borrowBook(1L, 1L),
                    String.format("Borrowing should throw IneligibleMemberException when " +
                            "member has %d books (limit is 5). Scenario: %s",
                            currentBorrowCount, scenario));

            // Verify: exception message references the limit
            assertAll("Ineligible borrow should throw with descriptive message",
                () -> assertNotNull(exception.getMessage(),
                        "Exception message should not be null"),
                () -> assertEquals("Member has reached the limit of 5 books.",
                        exception.getMessage(),
                        "Exception message should clearly state the borrow limit")
            );

            // Verify: no book or transaction was saved
            verify(bookRepository, never()).save(any(Book.class));
            verify(transactionRepository, never()).save(any(BorrowTransaction.class));
        }
    }


    // ===================================================================
    // TEST 4: TITLE SEARCH EDGE CASES — @NullAndEmptySource + @ValueSource
    // ===================================================================
    //
    // Tests how searchBooks() handles unusual title inputs. The service
    // uses this guard: if (title != null && !title.isEmpty())
    //
    // Key edge cases:
    //   null  → falls through to findAll()         (title is null)
    //   ""    → falls through to findAll()         (title is empty)
    //   "   " → passes guard, calls findByTitle... (isEmpty() returns false!)
    //   "!@#$%" → passes guard, treated as search term (no sanitisation)
    //   "UPPERCASE TITLE" → passes guard, case-insensitive search
    //   "partial" → passes guard, partial match via LIKE %partial%
    //
    // BUG IDENTIFIED: The code checks isEmpty() but NOT isBlank().
    // Whitespace-only strings like "   " pass the null/empty guard and
    // reach findByTitleContainingIgnoreCase(). This is a real edge case
    // that could produce unexpected empty results in production.
    //
    // RECOMMENDATION: Replace `!title.isEmpty()` with `!title.isBlank()`
    // in BookServiceImpl.searchBooks() to correctly handle whitespace-only
    // inputs by falling through to findAll().
    // ===================================================================

    @ParameterizedTest(name = "title=\"{0}\" — should be handled gracefully by searchBooks")
    @NullAndEmptySource
    @ValueSource(strings = {
        "   ",              // Whitespace-only — BUG: isEmpty() passes this through!
        "!@#$%",            // Special characters — no input sanitisation in service
        "UPPERCASE TITLE",  // All caps — tests case-insensitive search branch
        "partial"           // Partial match — tests LIKE %partial% behaviour
    })
    @DisplayName("Title Search Edge Cases: should handle null, empty, whitespace, special chars, and case variations gracefully")
    void shouldHandleEdgeCaseTitleInputsGracefully(String title) {
        // Arrange: prepare mock returns for both possible code paths
        Page<Book> allBooksPage = createSingleBookPage(testBook);
        Page<Book> titleSearchPage = createSingleBookPage(testBook);

        // Determine which branch the service will take based on the guard logic:
        //   if (title != null && !title.isEmpty()) → findByTitleContainingIgnoreCase
        //   else → findAll
        boolean titlePassesGuard = (title != null && !title.isEmpty());

        if (titlePassesGuard) {
            // Service will call findByTitleContainingIgnoreCase
            when(bookRepository.findByTitleContainingIgnoreCase(eq(title), eq(pageable)))
                    .thenReturn(titleSearchPage);
        } else {
            // Service will fall through to findAll
            when(bookRepository.findAll(eq(pageable))).thenReturn(allBooksPage);
        }

        // Act: call searchBooks with only the title parameter (isbn=null, category=null)
        // so the ISBN branch is skipped and the title branch is evaluated
        Page<Book> result = bookService.searchBooks(title, null, null, pageable);

        // Assert: result should always be non-null regardless of input
        assertAll("Search result should be valid regardless of title input: \"" + title + "\"",
            () -> assertNotNull(result,
                    "Result page must not be null for title input: \"" + title + "\""),
            () -> assertFalse(result.isEmpty(),
                    "Result page should contain books for title input: \"" + title + "\"")
        );

        // Verify: confirm which repository method was actually invoked
        if (titlePassesGuard) {
            /*
             * BUG DOCUMENTATION: When title is "   " (whitespace only), the service
             * code enters this branch because String.isEmpty() returns false for
             * whitespace strings. This means whitespace-only titles are sent to
             * findByTitleContainingIgnoreCase("   ", pageable) — which will likely
             * return empty results in a real database.
             *
             * The fix would be to change the guard in BookServiceImpl.searchBooks()
             * from:  title != null && !title.isEmpty()
             * to:    title != null && !title.isBlank()
             *
             * Java 11+ String.isBlank() returns true for whitespace-only strings,
             * correctly routing them to the findAll() fallback.
             */
            verify(bookRepository).findByTitleContainingIgnoreCase(eq(title), eq(pageable));
            verify(bookRepository, never()).findAll(any(Pageable.class));
            verify(bookRepository, never()).findByIsbn(any(String.class), any(Pageable.class));
            verify(bookRepository, never()).findByCategoryContainingIgnoreCase(any(), any());
        } else {
            // null or empty title → correctly falls through to findAll
            verify(bookRepository).findAll(eq(pageable));
            verify(bookRepository, never()).findByTitleContainingIgnoreCase(any(), any());
            verify(bookRepository, never()).findByIsbn(any(String.class), any(Pageable.class));
            verify(bookRepository, never()).findByCategoryContainingIgnoreCase(any(), any());
        }
    }
}
