package com.plog.plogbackend.domain.inquiry.contents;

public enum Category {
  REFUND("환불 문의"),
  SECESSION("회원/탈퇴 문의"),
  ERROR("오류 문의"),
  FEEDBACK("건의/제안"),
  ETC("기타");

  private final String description;

  Category(String description) {
    this.description = description;
  }

  public String getDescription() {
    return this.description;
  }
}
