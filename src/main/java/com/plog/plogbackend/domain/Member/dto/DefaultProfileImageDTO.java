package com.plog.plogbackend.domain.Member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "기본 프로필 이미지 정보")
public class DefaultProfileImageDTO {

  @Schema(description = "기본 이미지 식별키 (DB ID)", example = "1")
  private Long id;

  @Schema(description = "기본 이미지 URL", example = "https://storage.googleapis.com/.../default.png")
  private String imageUrl;
}
