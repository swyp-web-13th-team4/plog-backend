package com.plog.plogbackend.domain.Member.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
  private String nickname;
  private String profileImageUrl;
  private String introduction;
  private MemberBadgeResponse mainBadge;
}
