package com.lms.controller;

import com.lms.entity.BorrowTransaction;
import com.lms.entity.Member;
import com.lms.service.BorrowService;
import com.lms.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BorrowController {
    private final BorrowService borrowService;
    private final MemberService memberService;

    @Autowired
    public BorrowController(BorrowService borrowService, MemberService memberService) {
        this.borrowService = borrowService;
        this.memberService = memberService;
    }

    @PostMapping("/borrow/{bookId}")
    public ResponseEntity<BorrowTransaction> borrowBook(@PathVariable Long bookId, @RequestParam Long memberId) {
        return ResponseEntity.ok(borrowService.borrowBook(memberId, bookId));
    }

    @PostMapping("/return/{bookId}")
    public ResponseEntity<BorrowTransaction> returnBook(@PathVariable Long bookId, @RequestParam Long memberId) {
        return ResponseEntity.ok(borrowService.returnBook(memberId, bookId));
    }

    @GetMapping("/fines/{memberId}")
    public ResponseEntity<Map<String, Double>> getTotalFines(@PathVariable Long memberId) {
        Double total = borrowService.calculateTotalFines(memberId);
        return ResponseEntity.ok(Map.of("totalFines", total));
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<Member> getMemberProfile(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @GetMapping("/members/{id}/history")
    public ResponseEntity<List<BorrowTransaction>> getMemberHistory(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.getMemberHistory(id));
    }
}
