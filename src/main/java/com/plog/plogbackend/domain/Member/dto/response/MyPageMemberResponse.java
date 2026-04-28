package com.plog.plogbackend.domain.Member.dto.response;

import com.plog.plogbackend.domain.badge.entity.Badge;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageMemberResponse {
  private String nickname;
  private String profileImageUrl;
  private String introduction;
  private Badge mainBadge;
}
