package com.plog.plogbackend.domain.inquiry.controller.dto;

import com.plog.plogbackend.domain.inquiry.contents.Status;
import com.plog.plogbackend.domain.inquiry.entity.Inquiry;
import com.plog.plogbackend.domain.inquiry.entity.InquiryImages;
import java.time.LocalDateTime;
import java.util.List;

public record InquiryResponse(
    Long id,
    String title,
    String content,
    Status status,
    List<String> imageUrls,
    LocalDateTime localDateTime) {

  public InquiryResponse(Inquiry inquiry) {
    this(
        inquiry.getId(),
        inquiry.getTitle(),
        inquiry.getContent(),
        inquiry.getStatus(),
        inquiry.getImages().stream().map(InquiryImages::getImageUrl).toList(),
        inquiry.getCreatedAt());
  }

  public static InquiryResponse from(Inquiry inquiry) {
    return new InquiryResponse(inquiry);
  }
}
