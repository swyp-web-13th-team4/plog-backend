package com.plog.plogbackend.domain.image.controller;

import com.plog.plogbackend.domain.Member.service.MemberImageService;
import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.response.ApiResponse;
import com.plog.plogbackend.global.util.GcsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "테스트 - 이미지", description = "이미지 단독 업로드 테스트를 위한 컨트롤러")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/images")
public class TestImageController {

  private final GcsService gcsService;
  private final MemberImageService memberImageService;

  // ==========================================
  // 프로필 이미지 API
  // ==========================================

  @Operation(
      summary = "프로필 이미지 업로드/수정",
      description = "로그인한 회원의 프로필 이미지를 업로드합니다. 기존 이미지가 있으면 자동으로 교체됩니다.")
  @PutMapping(
      value = "/api/members/me/profile-image",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<ImageUrlResponse>> uploadProfileImage(
      Authentication authentication,
      @Parameter(description = "업로드할 이미지 파일 (jpg, png 등, 최대 10MB)") @RequestPart("image")
          MultipartFile image) {

    UUID memberKey = (UUID) authentication.getPrincipal();
    ImageUrlResponse response = memberImageService.uploadProfileImage(memberKey, image);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "프로필 이미지 삭제", description = "로그인한 회원의 프로필 이미지를 삭제하고 기본 이미지로 초기화합니다.")
  @DeleteMapping("/api/members/me/profile-image")
  public ResponseEntity<ApiResponse<Void>> deleteProfileImage(Authentication authentication) {
    UUID memberKey = (UUID) authentication.getPrincipal();
    memberImageService.deleteProfileImage(memberKey);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @Operation(
      summary = "[테스트] 다중 이미지 업로드",
      description = "게시글 컨텍스트 없이 이미지 여러 개가 GCS에 정상적으로 업로드되는지 테스트합니다 (최대5개).")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<List<ImageUrlResponse>>> uploadTestImages(
      @Parameter(description = "업로드할 이미지 파일들") @RequestPart("images") List<MultipartFile> images) {

    if (images != null && images.size() > 5) { // 이미지 업로드 5개로 제한
      throw new AppException(ErrorType.POST_IMAGE_LIMIT_EXCEEDED);
    }

    // 이미지 URL 리스트 반환 이걸 DB에 저장하면 될것 같습니다
    List<ImageUrlResponse> response =
        images.stream()
            .map(file -> new ImageUrlResponse(gcsService.upload(file, "test")))
            .toList();

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
