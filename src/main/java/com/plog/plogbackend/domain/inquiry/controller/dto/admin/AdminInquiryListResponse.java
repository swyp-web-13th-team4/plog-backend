package com.plog.plogbackend.domain.inquiry.controller.dto.admin;

import java.util.List;

public record AdminInquiryListResponse(
    List<AdminInquirysResponse> list, InquiryStatusListResponse statusCount) {

  public static AdminInquiryListResponse from(
      InquiryStatusListResponse counting, List<AdminInquirysResponse> response) {
    return new AdminInquiryListResponse(response, counting);
  }
}
