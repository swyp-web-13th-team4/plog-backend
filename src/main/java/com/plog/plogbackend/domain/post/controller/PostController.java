package com.plog.plogbackend.domain.post.controller;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.domain.post.service.PostImageService;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "게시글", description = "게시글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

  private final PostImageService postImageService;

  @Operation( // TODO : 예시 메서드 입니다. post api 개발 시작하면 삭제해주시면 됩니다.
      summary = "[테스트] 다중 이미지 업로드",
      description = "게시글 컨텍스트 없이 이미지 여러 개가 GCS에 정상적으로 업로드되는지 테스트합니다 (최대5개).")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<List<ImageUrlResponse>>> uploadTestImages(
      @Parameter(description = "업로드할 이미지 파일들") @RequestPart("images") List<MultipartFile> images) {

    List<ImageUrlResponse> response = postImageService.uploadTestImages(images);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
