package com.plog.plogbackend.domain.post.controller;

import com.plog.plogbackend.domain.post.controller.api.PostMapper;
import com.plog.plogbackend.domain.post.controller.dto.request.FeedFindRequest;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedDetailResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedResponse;
import com.plog.plogbackend.domain.post.service.FeedService;
import com.plog.plogbackend.domain.post.service.dto.FeedDetailCommand;
import com.plog.plogbackend.domain.post.service.dto.FeedFindCommand;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FeedController {

  private final FeedService feedService;

  // 전체 조회
  @Operation(summary = "피드 전체 조회")
  @GetMapping("/feed/list")
  public ResponseEntity<ApiResponse<FeedResponse>> feedList(FeedFindRequest request) {

    FeedFindCommand command = PostMapper.from(request);

    FeedResponse feedResponse = feedService.feedFind(command);

    ApiResponse<FeedResponse> success = ApiResponse.success(feedResponse);

    return ResponseEntity.ok().body(success);
  }

  // 상세 조회
  @Operation(summary = "피드 상세 조회")
  @GetMapping("/feed/{postId}")
  public ResponseEntity<ApiResponse<FeedDetailResponse>> feedDetail(
      @PathVariable("postId") Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {

    FeedDetailCommand command = PostMapper.from(id);
    FeedDetailResponse response = feedService.feedDetail(command, memberKey);
    ApiResponse<FeedDetailResponse> success = ApiResponse.success(response);

    return ResponseEntity.ok().body(success);
  }
}
