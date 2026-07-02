package com.plog.plogbackend.domain.inquiry.controller;

import com.plog.plogbackend.domain.inquiry.controller.dto.CreateInquiryRequest;
import com.plog.plogbackend.domain.inquiry.controller.dto.CreateInquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.InquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.InquirysResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.UpdateInquiryRequest;
import com.plog.plogbackend.domain.inquiry.service.InquiryService;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiry")
public class InquiryController {

  private final InquiryService inquiryService;

  @PostMapping("/new")
  public ResponseEntity<ApiResponse<CreateInquiryResponse>> create(
      @Parameter(description = "1:1문의 텍스트 데이터") @Valid @RequestPart("request")
          CreateInquiryRequest request,
      @Parameter(description = "첨부 이미지 ") @RequestPart(value = "images", required = false)
          List<MultipartFile> images,
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {
    long inquiryId = inquiryService.createInquiry(memberKey, request, images);

    CreateInquiryResponse response = CreateInquiryResponse.of(inquiryId);
    ApiResponse<CreateInquiryResponse> result = ApiResponse.success(response);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<InquirysResponse>>> List(
      @AuthenticationPrincipal UUID memberKey) {

    List<InquirysResponse> response = inquiryService.findInquirys(memberKey);

    ApiResponse<List<InquirysResponse>> success = ApiResponse.success(response);

    return ResponseEntity.ok().body(success);
  }

  @GetMapping("{id}")
  public ResponseEntity<ApiResponse<InquiryResponse>> Inquiry(@PathVariable Long id) {

    InquiryResponse inquiry = inquiryService.findInquiry(id);
    ApiResponse<InquiryResponse> success = ApiResponse.success(inquiry);

    return ResponseEntity.ok().body(success);
  }

  @PatchMapping("{id}")
  public ResponseEntity<ApiResponse<Void>> update(
      @PathVariable Long id,
      @Valid UpdateInquiryRequest request,
      List<MultipartFile> images,
      @AuthenticationPrincipal UUID memberKey) {

    inquiryService.updateInquiry(id, memberKey, request, images);

    return ResponseEntity.ok().body(ApiResponse.success(null));
  }

  @DeleteMapping("{id}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable Long id, @AuthenticationPrincipal UUID memberKey) {
    inquiryService.deleteInquiry(id, memberKey);

    return ResponseEntity.ok().body(ApiResponse.success(null));
  }
}
