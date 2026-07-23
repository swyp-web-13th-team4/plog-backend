package com.plog.plogbackend.domain.inquiry.controller;

import com.plog.plogbackend.domain.inquiry.controller.dto.user.CreateInquiryRequest;
import com.plog.plogbackend.domain.inquiry.controller.dto.user.CreateInquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.user.InquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.user.InquirysResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.user.UpdateInquiryRequest;
import com.plog.plogbackend.domain.inquiry.service.InquiryService;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Inquiry", description = "1:1문의 사용자 페이지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiry")
public class InquiryController {

  private final InquiryService inquiryService;

  @Operation(summary = "1:1문의 등록")
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

  @Operation(summary = "1:1문의 목록")
  @GetMapping
  public ResponseEntity<ApiResponse<List<InquirysResponse>>> List(
      @AuthenticationPrincipal UUID memberKey) {

    List<InquirysResponse> response = inquiryService.findInquirys(memberKey);

    ApiResponse<List<InquirysResponse>> success = ApiResponse.success(response);

    return ResponseEntity.ok().body(success);
  }

  @Operation(summary = "문의 상세보기")
  @GetMapping("{id}")
  public ResponseEntity<ApiResponse<InquiryResponse>> Inquiry(@PathVariable Long id) {

    InquiryResponse inquiry = inquiryService.findInquiry(id);
    ApiResponse<InquiryResponse> success = ApiResponse.success(inquiry);

    return ResponseEntity.ok().body(success);
  }

  @Operation(summary = "문의 수정")
  @PatchMapping("{id}")
  public ResponseEntity<ApiResponse<Void>> update(
      @PathVariable Long id,
      @Valid UpdateInquiryRequest request,
      List<MultipartFile> images,
      @AuthenticationPrincipal UUID memberKey) {

    inquiryService.updateInquiry(id, memberKey, request, images);

    return ResponseEntity.ok().body(ApiResponse.success(null));
  }

  @Operation(summary = "문의 삭제")
  @DeleteMapping("{id}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable Long id, @AuthenticationPrincipal UUID memberKey) {
    inquiryService.deleteInquiry(id, memberKey);

    return ResponseEntity.ok().body(ApiResponse.success(null));
  }
}
