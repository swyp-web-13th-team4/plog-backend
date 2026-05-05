package com.plog.plogbackend.domain.map.model;

public enum RecordSortType {
  LATEST,
  RECORD_COUNT;

  public static RecordSortType from(String value) {
    return switch (value) {
      case "저장 개수순" -> RECORD_COUNT;
      default -> LATEST;
    };
  }
}
