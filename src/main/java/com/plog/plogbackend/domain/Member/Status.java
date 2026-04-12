package com.plog.plogbackend.domain.Member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status { // soft delete 속성
  ACTIVE("활성/정상"),
  DELETED("삭제됨");

  // 나중에 쓰일 수도 있는 고도화 기능
  //    INACTIVE("비활성/휴면"),
  //    SUSPENDED("정지/숨김"); // 회원은 '정지', 게시글은 '신고로 인한 숨김' 등등 활용

  private final String description;
}
