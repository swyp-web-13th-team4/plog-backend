package com.plog.plogbackend.domain.Member.dto.response;

import com.plog.plogbackend.domain.Member.entity.WorkTypeCard;
import io.swagger.v3.oas.annotations.media.Schema;

/** 작업 유형 카드 응답 DTO */
@Schema(description = "작업 유형 카드 정보")
public record WorkTypeCardResponse(
    @Schema(description = "카드 ID") Long id,
    @Schema(description = "카드 이미지 URL") String imageUrl,
    @Schema(description = "카드명") String name,
    @Schema(description = "카드 설명") String description) {

  public static WorkTypeCardResponse from(WorkTypeCard card) {
    return new WorkTypeCardResponse(card.getId(), card.getImageUrl(), card.getName(),
        card.getDescription());
  }
}
