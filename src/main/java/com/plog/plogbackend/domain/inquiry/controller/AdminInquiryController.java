package com.plog.plogbackend.domain.inquiry.controller;

import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminAnswerInquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminInquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminInquirysResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AnswerInquiryRequest;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.InquiryStatusListResponse;
import com.plog.plogbackend.domain.inquiry.service.AdminInquiryService;
import com.plog.plogbackend.domain.inquiry.service.InquiryService;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AdminInquiry", description = "관리자 1:1문의 페이지")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/inquiry")
public class AdminInquiryController {

  private final AdminInquiryService adminInquiryService;
  private final InquiryService inquiryService;

  @Operation(summary = "문의 목록")
  @GetMapping
  public ResponseEntity<ApiResponse<List<AdminInquirysResponse>>> List(
      @AuthenticationPrincipal UUID memberKey) {

    List<AdminInquirysResponse> response = adminInquiryService.findInquirys(memberKey);

    ApiResponse<List<AdminInquirysResponse>> success = ApiResponse.success(response);

    return ResponseEntity.ok().body(success);
  }

  @Operation(summary = "카테고리별 문의 수")
  @GetMapping("/count")
  public ResponseEntity<ApiResponse<InquiryStatusListResponse>> count(
      @AuthenticationPrincipal UUID memberKey) {

    InquiryStatusListResponse response = adminInquiryService.inquiryCount(memberKey);

    ApiResponse<InquiryStatusListResponse> success = ApiResponse.success(response);

    return ResponseEntity.ok().body(success);
  }

  @Operation(summary = "상세 조회")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<AdminInquiryResponse>> Inquiry(
      @PathVariable Long id, @AuthenticationPrincipal UUID memberKey) {

    AdminInquiryResponse inquiry = adminInquiryService.findInquiry(id, memberKey);
    ApiResponse<AdminInquiryResponse> success = ApiResponse.success(inquiry);

    return ResponseEntity.ok().body(success);
  }

  @Operation(summary = "답글 남기기")
  @PostMapping("/{id}")
  public ResponseEntity<ApiResponse<AdminAnswerInquiryResponse>> answer(
      @PathVariable Long id,
      AnswerInquiryRequest request,
      @AuthenticationPrincipal UUID memberKey) {

    AdminAnswerInquiryResponse response = adminInquiryService.createAnswer(id, request, memberKey);
    ApiResponse<AdminAnswerInquiryResponse> result = ApiResponse.success(response);

    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }
}
