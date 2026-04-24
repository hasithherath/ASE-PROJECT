package com.lms.security;

import com.lms.entity.Book;
import com.lms.entity.Member;
import com.lms.entity.User;
import com.lms.repository.BookRepository;
import com.lms.repository.MemberRepository;
import com.lms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, 
                           MemberRepository memberRepository, 
                           BookRepository bookRepository, 
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Checking database state...");

        // Create Librarian if not exists
        if (!userRepository.findByUsername("librarian").isPresent()) {
            User librarianUser = new User();
            librarianUser.setUsername("librarian");
            librarianUser.setPassword(passwordEncoder.encode("password123"));
            librarianUser.setRole("ROLE_LIBRARIAN");
            userRepository.save(librarianUser);

            Member librarianMember = new Member();
            librarianMember.setFirstName("Admin");
            librarianMember.setLastName("User");
            librarianMember.setEmail("admin@lms.com");
            librarianMember.setUser(librarianUser);
            memberRepository.save(librarianMember);
            System.out.println("Created librarian user.");
        }

        // Create Member if not exists
        if (!userRepository.findByUsername("member1").isPresent()) {
            User member1User = new User();
            member1User.setUsername("member1");
            member1User.setPassword(passwordEncoder.encode("password123"));
            member1User.setRole("ROLE_MEMBER");
            userRepository.save(member1User);

            Member member1 = new Member();
            member1.setFirstName("John");
            member1.setLastName("Doe");
            member1.setEmail("john@example.com");
            member1.setUser(member1User);
            memberRepository.save(member1);
            System.out.println("Created member1 user.");
        }

        // Create Books if they don't exist (checking by ISBN)
        saveBookIfNotExists("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", "Classic");
        saveBookIfNotExists("1984", "George Orwell", "9780451524935", "Dystopian");
        saveBookIfNotExists("To Kill a Mockingbird", "Harper Lee", "9780061120084", "Classic");
        saveBookIfNotExists("The Hobbit", "J.R.R. Tolkien", "9780547928227", "Fantasy");
        saveBookIfNotExists("Animal Farm", "George Orwell", "9780451526342", "Satire");

        System.out.println("==============================================");
        System.out.println("LMS INITIALIZATION READY");
        System.out.println("Login: librarian / password123");
        System.out.println("==============================================");
    }

    private void saveBookIfNotExists(String title, String author, String isbn, String category) {
        if (!bookRepository.findByIsbn(isbn).isPresent()) {
            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setIsbn(isbn);
            book.setCategory(category);
            book.setAvailableCopies(5);
            bookRepository.save(book);
            System.out.println("Created book: " + title);
        }
    }
}
