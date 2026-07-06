package com.plog.plogbackend.domain.inquiry.controller.dto.admin;

public record InquiryStatusListResponse(Long totalCount, Long receipt,Long waits, Long finish) {

    public static InquiryStatusListResponse from(long total, long receiptCount, long waitCount, long finishCount) {

       return new InquiryStatusListResponse(total,receiptCount,waitCount,finishCount);
    }
}
