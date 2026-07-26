package com.plog.plogbackend.domain.inquiry.controller;

import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminAnswerInquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminInquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminInquirysResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AnswerInquiryRequest;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.InquiryStatusListResponse;
import com.plog.plogbackend.domain.inquiry.service.AdminInquiryService;
import com.plog.plogbackend.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/inquiry")
@RequiredArgsConstructor
public class AdminInquiryController {

  private final AdminInquiryService adminInquiryService;

  @GetMapping
  public ApiResponse<List<AdminInquirysResponse>> list(@AuthenticationPrincipal UUID memberKey) {
    return ApiResponse.success(adminInquiryService.findInquirys(memberKey));
  }

  @GetMapping("/count")
  public ApiResponse<InquiryStatusListResponse> count(@AuthenticationPrincipal UUID memberKey) {
    return ApiResponse.success(adminInquiryService.inquiryCount(memberKey));
  }

  @GetMapping("/{id}")
  public ApiResponse<AdminInquiryResponse> inquiry(@PathVariable Long id,@AuthenticationPrincipal UUID memberKey) {
    return ApiResponse.success(adminInquiryService.findInquiry(id,memberKey));
  }

  @PostMapping("/{id}")
  public ApiResponse<AdminAnswerInquiryResponse> answer(
          @PathVariable Long id,
          @RequestBody @Valid AnswerInquiryRequest request,
          @AuthenticationPrincipal UUID memberKey) {
    return ApiResponse.success(adminInquiryService.createAnswer(id, request, memberKey));
  }
}