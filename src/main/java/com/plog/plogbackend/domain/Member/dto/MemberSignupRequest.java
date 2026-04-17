package com.plog.plogbackend.domain.Member.dto;

public record MemberSignupRequest(
        String nickname,
    boolean marketingAgreed) {}
