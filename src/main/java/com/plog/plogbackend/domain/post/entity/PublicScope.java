package com.plog.plogbackend.domain.post.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;

public enum PublicScope {
  PUBLIC,
  PRIVATE;

  @JsonCreator
  public static PublicScope from(String value) {
    if (value == null || value.isBlank()) {
      throw new AppException(ErrorType.INVALID_ACCESS_PATH);
    }
    try {
      return valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new AppException(ErrorType.INVALID_ACCESS_PATH);
    }
  }
}
