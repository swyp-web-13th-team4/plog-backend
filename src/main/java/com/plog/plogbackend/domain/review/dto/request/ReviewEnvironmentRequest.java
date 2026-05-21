package com.plog.plogbackend.domain.review.dto.request;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.EnumMap;
import java.util.Map;

public record ReviewEnvironmentRequest(
    @Schema(description = "공간 크기 점수", example = "5") @NotNull @Min(1) @Max(5) Integer spaceSize,
    @Schema(description = "소음 수준 점수", example = "4") @NotNull @Min(1) @Max(5) Integer noiseLevel,
    @Schema(description = "혼잡도 점수", example = "3") @NotNull @Min(1) @Max(5) Integer congestionLevel,
    @Schema(description = "집중도 점수", example = "5") @NotNull @Min(1) @Max(5) Integer focusLevel) {

  public Map<ReviewEnvironmentName, Integer> toMap() {
    Map<ReviewEnvironmentName, Integer> environments = new EnumMap<>(ReviewEnvironmentName.class);
    environments.put(ReviewEnvironmentName.SPACE_SIZE, spaceSize);
    environments.put(ReviewEnvironmentName.NOISE_LEVEL, noiseLevel);
    environments.put(ReviewEnvironmentName.CONGESTION_LEVEL, congestionLevel);
    environments.put(ReviewEnvironmentName.FOCUS_LEVEL, focusLevel);
    return environments;
  }
}
