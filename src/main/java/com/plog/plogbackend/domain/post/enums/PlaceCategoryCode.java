package com.plog.plogbackend.domain.post.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceCategoryCode {
  CAFE("카페", "cafe"),
  STUDY_CAFE("스터디 카페", "study-cafe"),
  LIBRARY("도서관", "library"),
  OFFICE("사무실", "office"),
  SHARED_OFFICE("공유 오피스", "shared-office"),
  ETC("기타 장소", "etc");

  private final String label;
  private final String value;

  /**
   * @JsonValue: 서버에서 프론트엔드로 응답(Response)을 줄 때, Enum 이름(STUDY_CAFE) 대신 value 필드("study-cafe")로
   * 직렬화하도록 합니다.
   */
  @JsonValue
  public String getValue() {
    return value;
  }

  /**
   * @JsonCreator: 프론트엔드에서 "study-cafe"라는 문자열로 요청(Request)이 오면, 알맞은 Enum 상수(STUDY_CAFE)로
   * 역직렬화(매핑)해줍니다.
   */
  @JsonCreator
  public static PlaceCategoryCode fromValue(String value) {
    return findByValue(value)
        .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 장소 카테고리입니다: " + value));
  }

  public static Optional<PlaceCategoryCode> findByValue(String value) {
    return Arrays.stream(values())
        .filter(category -> category.getValue().equals(value))
        .findFirst();
  }
}
