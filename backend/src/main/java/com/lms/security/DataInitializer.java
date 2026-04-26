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
        // Create 100 Members for Load Testing
        for (int i = 1; i <= 100; i++) {
            String username = "member" + i;
            if (!userRepository.findByUsername(username).isPresent()) {
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode("password123"));
                user.setRole("ROLE_MEMBER");
                userRepository.save(user);

                Member member = new Member();
                member.setFirstName("Member");
                member.setLastName(String.valueOf(i));
                member.setEmail(username + "@lms.com");
                member.setUser(user);
                memberRepository.save(member);
            }
        }

        // Create 100 Books for Load Testing
        for (int i = 1; i <= 100; i++) {
            String title = "Test Book " + i;
            String isbn = "ISBN-" + i;
            saveBookIfNotExists(title, "Test Author", isbn, "Test Category");
        }

        System.out.println("==============================================");
        System.out.println("LMS INITIALIZATION READY: 100 USERS & 100 BOOKS SEEDED");
        System.out.println("Login: member1 / password123");
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
