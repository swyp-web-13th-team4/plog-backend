package com.plog.plogbackend.domain.inquiry.controller.dto.admin;

import com.plog.plogbackend.domain.inquiry.contents.Category;
import com.plog.plogbackend.domain.inquiry.contents.Status;
import com.plog.plogbackend.domain.inquiry.entity.Inquiry;
import java.time.LocalDateTime;
import java.util.List;

public record AdminInquirysResponse(Long id, String title, String author, Category category,
                                    LocalDateTime localDateTime,
                                    Status inquiryStatus) {


    public AdminInquirysResponse(Inquiry inquiry) {
        this(
                inquiry.getId(),
                inquiry.getTitle(),
                inquiry.getMember().getNickname(),
                inquiry.getCategory(),
                inquiry.getCreatedAt(),
                inquiry.getInquiryStatus()
        );
    }

    public static List<AdminInquirysResponse> from(List<Inquiry> inquiries) {

        return inquiries.stream().map(AdminInquirysResponse::new).toList();
    }
}
