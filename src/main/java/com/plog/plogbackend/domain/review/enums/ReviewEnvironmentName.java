package com.plog.plogbackend.domain.review.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewEnvironmentName {

  /**
   * 프론트 요청 api { "environments": { "spaceSize": 5, "noiseLevel": 4, "congestionLevel": 3,
   * "focusLevel": 5 } }
   */
  SPACE_SIZE(
      "spaceSize",
      "공간 크기",
      "company-filled",
      Map.of(
          5, "매우 넓어요",
          4, "넓은 편이에요",
          3, "보통이에요",
          2, "좁은 편이에요",
          1, "매우 좁아요")),

  NOISE_LEVEL(
      "noiseLevel",
      "소음 수준",
      "megaphone-filled",
      Map.of(
          5, "매우 조용해요",
          4, "조용한 편이에요",
          3, "보통이에요",
          2, "시끄러운 편이에요",
          1, "매우 시끄러워요")),

  CONGESTION_LEVEL(
      "congestionLevel",
      "혼잡도",
      "smile-filled",
      Map.of(
          5, "여유로워요",
          4, "여유 있는 편이에요",
          3, "보통이에요",
          2, "붐비는 편이에요",
          1, "매우 붐벼요")),

  FOCUS_LEVEL(
      "focusLevel",
      "집중도",
      "fire-filled",
      Map.of(
          5, "매우 잘 돼요",
          4, "잘 되는 편이에요",
          3, "보통이에요",
          2, "잘 안 돼요",
          1, "전혀 안 돼요"));

  private final String value;
  private final String title;
  private final String iconName;
  private final Map<Integer, String> labels;

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static ReviewEnvironmentName fromValue(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return Arrays.stream(values())
        .filter(environment -> environment.value.equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 리뷰 환경 항목입니다: " + value));
  }

  public String getLabel(int score) {
    String label = labels.get(score);
    if (label == null) {
      throw new IllegalArgumentException("지원하지 않는 리뷰 환경 점수입니다: " + score);
    }
    return label;
  }
}
