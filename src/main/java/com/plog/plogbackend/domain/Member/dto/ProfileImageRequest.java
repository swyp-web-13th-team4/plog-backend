package com.plog.plogbackend.domain.Member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "프로필 이미지 URL 선택 요청")
public class ProfileImageRequest {
  @Schema(
      description = "선택한 프로필 이미지 URL",
      example = "https://storage.googleapis.com/plog-bucket/profiles/default/profile1.png")
  private String imageUrl;

  public ProfileImageRequest(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}
