package com.plog.plogbackend.domain.inquiry.contents;

public enum Status {
  RECEIPT("문의 접수"),
  WAIT("답변 대기"),
  FINISH("답변 완료");

  private final String description;

  Status(String description) {
    this.description = description;
  }

  public String getDescription() {
    return this.description;
  }
}
