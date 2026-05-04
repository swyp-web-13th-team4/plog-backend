package com.plog.plogbackend.domain.post.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PublicScope {
  PUBLIC,
  PRIVATE;

  @JsonCreator
  public static PublicScope from(String value) {
    return valueOf(value.toUpperCase());
  }
}
