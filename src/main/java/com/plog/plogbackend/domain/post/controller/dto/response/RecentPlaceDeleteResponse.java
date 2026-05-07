package com.plog.plogbackend.domain.post.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최근 장소 삭제 갯수 응답")
public record RecentPlaceDeleteResponse(
    @Schema(description = "삭제된 항목 개수", example = "7") int deletedCount) {}
