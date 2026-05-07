package com.plog.plogbackend.domain.post.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceCategoryCode {
  CAFE("카페"),
  STUDY_CAFE("스터디 카페"),
  LIBRARY("도서관"),
  OFFICE("사무실"),
  SHARED_OFFICE("공유 오피스"),
  ETC("기타 장소");

  private final String label;

  @JsonCreator
  public static PlaceCategoryCode from(String value) {
    if (value == null || value.isBlank()) {
      throw new AppException(ErrorType.CATEGORY_NOT_FOUND);
    }
    try {
      return valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new AppException(ErrorType.CATEGORY_NOT_FOUND);
    }
  }
}
