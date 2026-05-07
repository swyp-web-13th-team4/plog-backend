package com.plog.plogbackend.domain.post.controller.dto.request.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceRequest(
    @Schema(description = "name은 공백을 허용하지 않습니다.") @NotBlank String name,
    @Schema(description = "address는 공백을 허용하지 않습니다.") @NotBlank String address,
    @Schema(description = "latitud는 필수 값입니다.") @NotNull Double latitude,
    @Schema(description = "longitud는 필수 값입니다.") @NotNull Double longitude) {}
