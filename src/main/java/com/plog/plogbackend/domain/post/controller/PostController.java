package com.plog.plogbackend.domain.post.controller;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.domain.post.controller.api.PostMapper;
import com.plog.plogbackend.domain.post.controller.dto.request.post.PostCreateRequest;
import com.plog.plogbackend.domain.post.controller.dto.response.PostCreateResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.PostResponse;
import com.plog.plogbackend.domain.post.service.PostImageService;
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
    private final PostService postService;

    @Operation(summary = "게시글 생성", description = "게시글 정보와 이미지를 함께 업로드합니다. (이미지 최대 5개)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
            @Parameter(description = "게시글 텍스트 데이터") @Valid @RequestPart("texts")
            PostCreateRequest request,
            @Parameter(description = "게시글 이미지 (최대 5개)") @RequestPart(value = "images")
            List<MultipartFile> images,
            @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {

        PostCreateCommand command = PostMapper.from(request, memberKey);
        PostResponse postResponse = postService.create(command);
        List<ImageUrlResponse> imageResponse =
                postImageService.uploadPostImages(postResponse.postId(), images);

        PostCreateResponse postCreateResponse = new PostCreateResponse(postResponse, imageResponse);
        return ResponseEntity.ok(ApiResponse.success(postCreateResponse));
    }

  /*
      @Operation( // TODO : 예시 메서드 입니다. post api 개발 시작하면 삭제해주시면 됩니다.
        summary = "[테스트] 다중 이미지 업로드",
        description = "게시글 컨텍스트 없이 이미지 여러 개가 GCS에 정상적으로 업로드되는지 테스트합니다 (최대5개).")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ImageUrlResponse>>> uploadTestImages(
        @Parameter(description = "업로드할 이미지 파일들") @RequestPart("images") List<MultipartFile> images) {

      List<ImageUrlResponse> response = postImageService.uploadTestImages(images);

      return ResponseEntity.ok(ApiResponse.success(response));
    }
  */
}
