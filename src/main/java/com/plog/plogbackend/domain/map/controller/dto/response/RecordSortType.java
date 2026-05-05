package com.plog.plogbackend.domain.map.controller.dto.response;

public enum RecordSortType {
  LATEST,
  FOCUS,
  STUDY_TIME;

  public static RecordSortType from(String value) {
    return switch (value) {
      case "집중도순" -> FOCUS;
      case "작업시간순" -> STUDY_TIME;
      default -> LATEST;
    };
  }
}
