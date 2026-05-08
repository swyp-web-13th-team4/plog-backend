package com.plog.plogbackend.domain.post.controller;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.domain.post.controller.api.PostMapper;
import com.plog.plogbackend.domain.post.controller.dto.request.post.PostCreateRequest;
import com.plog.plogbackend.domain.post.controller.dto.response.PostCreateResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.PostQueryResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.PostTextResponse;
import com.plog.plogbackend.domain.post.service.PostImageService;
import com.plog.plogbackend.domain.post.service.PostQueryService;
import com.plog.plogbackend.domain.post.service.PostService;
import com.plog.plogbackend.domain.post.service.dto.PostCreateCommand;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "게시글", description = "게시글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

  private final PostImageService postImageService;
  private final PostService postService;
  private final PostQueryService postQueryService;

  @Operation(summary = "게시글 생성", description = "게시글 정보와 이미지를 함께 업로드합니다. (이미지 최대 5개)")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
      @Parameter(description = "게시글 텍스트 데이터") @Valid @RequestPart("request")
          PostCreateRequest request,
      @Parameter(description = "게시글 이미지 (최대 5개)") @RequestPart(value = "images")
          List<MultipartFile> images,
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {

    PostCreateCommand command = PostMapper.from(request, memberKey);

    // Text 관련 처리
    PostTextResponse postTextResponse = postService.create(command);

    // image 관련 처리
    List<ImageUrlResponse> imageResponse =
        postImageService.uploadPostImages(postTextResponse.postId(), images);

    return ResponseEntity.ok(
        ApiResponse.success(PostCreateResponse.of(postTextResponse, imageResponse)));
  }

  @GetMapping("/{postId}/edit")
  public ResponseEntity<ApiResponse<PostQueryResponse>> getPost(
      @PathVariable Long postId, @AuthenticationPrincipal UUID memberKey) {
    PostQueryResponse post = postQueryService.getPost(postId, memberKey);
    return ResponseEntity.ok(ApiResponse.success(post));
  }
}
