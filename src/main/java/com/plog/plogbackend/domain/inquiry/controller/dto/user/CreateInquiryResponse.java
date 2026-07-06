package com.plog.plogbackend.domain.inquiry.controller.dto.user;

public record CreateInquiryResponse(Long InquiryId) {

  public static CreateInquiryResponse of(long inquiryId) {

    return new CreateInquiryResponse(inquiryId);
  }
}
