package com.plog.plogbackend.domain.map.model;

public enum SortType {
  LATEST,
  RECORD_COUNT,
  STUDY_TIME,
  FOCUS;

  public static SortType from(String value) {
    return switch (value) {
      case "저장 개수순" -> RECORD_COUNT;
      case "작업 시간순" -> STUDY_TIME;
      case "집중도순" -> FOCUS;
      default -> LATEST;
    };
  }
}
