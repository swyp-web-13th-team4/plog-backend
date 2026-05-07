package com.plog.plogbackend.domain.post.controller.dto.request.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecentPlaceSaveRequest(
    @NotBlank(message = "장소명은 필수입니다.") @Size(max = 100, message = "장소명은 100자를 넘을 수 없습니다.")
        String placeName,
    @NotBlank(message = "주소는 필수입니다.") @Size(max = 200) String address,
    @NotNull Double latitude,
    @NotNull Double longitude) {}
