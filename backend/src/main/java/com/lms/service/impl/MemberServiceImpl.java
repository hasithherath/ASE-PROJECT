package com.lms.service.impl;

import com.lms.entity.Member;
import com.lms.entity.User;
import com.lms.repository.MemberRepository;
import com.lms.repository.UserRepository;
import com.lms.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository, UserRepository userRepository) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    @Override
    public Member getMemberByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return memberRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Member record not found for user"));
    }
}
