package com.plog.plogbackend.domain.inquiry.controller.dto.admin;

import com.plog.plogbackend.domain.inquiry.contents.Status;
import com.plog.plogbackend.domain.inquiry.entity.Inquiry;
import java.time.LocalDateTime;

public record AdminAnswerInquiryResponse(
    Long inquiryId,
    String answerTitle,
    String answerContent,
    Status status,
    LocalDateTime answeredAt) {

  public static AdminAnswerInquiryResponse from(Inquiry inquiry) {
    return new AdminAnswerInquiryResponse(
        inquiry.getId(),
        inquiry.getAnswerTitle(),
        inquiry.getAnswerContent(),
        inquiry.getInquiryStatus(),
        inquiry.getAnswerTime());
  }
}
