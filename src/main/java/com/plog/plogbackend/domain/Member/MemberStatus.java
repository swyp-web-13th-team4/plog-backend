package com.plog.plogbackend.domain.Member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus { // soft delete 속성

    ACTIVE("정상 작동중인 활성 계정"),
//    INACTIVE("장기 미접속으로 인한 휴면 계정"), // 고도화 기능
//    SUSPENDED("운영원칙 위반으로 인한 이용 정지 계정"),
    DELETED("사용자가 탈퇴를 요청한 계정");

    private final String description;
}