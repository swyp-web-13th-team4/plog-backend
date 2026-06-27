package com.plog.plogbackend.domain.inquiry.controller.dto;

import com.plog.plogbackend.domain.inquiry.contents.Category;
import com.plog.plogbackend.domain.inquiry.entity.Inquiry;
import com.plog.plogbackend.domain.member.Member;

public record CreateInquiryRequest(Category category, String title, String content) {

  public Inquiry toEntity(Member member) {

    return new Inquiry(this.category, this.title, this.content, member);
  }
}
