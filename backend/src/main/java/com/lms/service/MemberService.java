package com.lms.service;

import com.lms.entity.Member;

public interface MemberService {
    Member getMemberById(Long id);
    Member getMemberByUserId(Long userId);
}
