package com.plog.plogbackend.domain.inquiry.controller.dto.admin;

import com.plog.plogbackend.domain.inquiry.contents.Status;
import com.plog.plogbackend.domain.inquiry.entity.Inquiry;
import com.plog.plogbackend.domain.inquiry.entity.InquiryImages;
import java.time.LocalDateTime;
import java.util.List;

public record AdminInquiryResponse(
    Long id,
    String title,
    String content,
    String author,
    String memberId,
    Status status,
    List<String> imageUrls,
    LocalDateTime localDateTime,
    String answerTitle,
    String answerContent,
    LocalDateTime answeredAt) {


  public AdminInquiryResponse(Inquiry inquiry) {
    this(
        inquiry.getId(),
        inquiry.getTitle(),
        inquiry.getContent(),
        inquiry.getMember().getNickname(),
        inquiry.getMember().getProviderId(),
        inquiry.getInquiryStatus(),
        inquiry.getImages().stream().map(InquiryImages::getImageUrl).toList(),
        inquiry.getCreatedAt(),
            inquiry.getAnswerTitle(),
            inquiry.getAnswerContent(),
            inquiry.getAnswerTime());
  }

  public static AdminInquiryResponse from(Inquiry inquiry) {
    return new AdminInquiryResponse(inquiry);
  }
}
