package com.plog.plogbackend.domain.Member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageMemberResponse {
  private String nickname;
  private String profileImageUrl;
  private String introduction;
  // TODO : 대표 뱃지 or 대표 뱃지 + 모든 뱃지
}
