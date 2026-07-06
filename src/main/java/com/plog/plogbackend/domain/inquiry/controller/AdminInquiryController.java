package com.plog.plogbackend.domain.inquiry.controller;

import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminAnswerInquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminInquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminInquirysResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.InquiryStatusListResponse;
import com.plog.plogbackend.domain.inquiry.service.AdminInquiryService;
import com.plog.plogbackend.domain.inquiry.service.InquiryService;
import com.plog.plogbackend.global.response.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminInquiryController {

    private final AdminInquiryService AdmininquiryService;
    private final InquiryService inquiryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminInquirysResponse>>> List(
            @AuthenticationPrincipal UUID memberKey) {

        List<AdminInquirysResponse> response = AdmininquiryService.findInquirys(memberKey);

        ApiResponse<List<AdminInquirysResponse>> success = ApiResponse.success(response);

        return ResponseEntity.ok().body(success);
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<InquiryStatusListResponse>> count(
            @AuthenticationPrincipal UUID memberKey) {

        InquiryStatusListResponse response = AdmininquiryService.inquiryCount(memberKey);

        ApiResponse<InquiryStatusListResponse> success = ApiResponse.success(response);

        return ResponseEntity.ok().body(success);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminInquiryResponse>> Inquiry(@PathVariable Long id) {

        AdminInquiryResponse inquiry = AdmininquiryService.findInquiry(id);
        ApiResponse<AdminInquiryResponse> success = ApiResponse.success(inquiry);

        return ResponseEntity.ok().body(success);
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminAnswerInquiryResponse>> answer(@PathVariable Long id) {

    }

}
