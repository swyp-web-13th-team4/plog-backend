package com.plog.plogbackend.domain.inquiry.controller.dto;

import com.plog.plogbackend.domain.inquiry.contents.Status;
import com.plog.plogbackend.domain.inquiry.entity.Inquiry;
import java.time.LocalDateTime;
import java.util.List;

public record InquirysResponse(Long id, String title, Status status, LocalDateTime localDateTime) {

  public InquirysResponse(Inquiry inquiry) {
    this(inquiry.getId(), inquiry.getTitle(), inquiry.getStatus(), inquiry.getCreatedAt());
  }

  public static List<InquirysResponse> from(List<Inquiry> inquiries) {

    return inquiries.stream().map(InquirysResponse::new).toList();
  }
}
