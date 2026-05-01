package com.plog.plogbackend.domain.post.controller;

import com.plog.plogbackend.domain.post.controller.api.PostMapper;
import com.plog.plogbackend.domain.post.controller.dto.request.FeedFindRequest;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedDetailResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.UpdateBookMarked;
import com.plog.plogbackend.domain.post.controller.dto.response.UpdateLiked;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FeedController {

  private final FeedService feedService;

  // 전체 조회
  @Operation(summary = "피드 전체 조회")
  @GetMapping("/feed/list")
  public ResponseEntity<ApiResponse<FeedResponse>> feedList(
      FeedFindRequest request, @AuthenticationPrincipal UUID memberKey) {

    FeedFindCommand command = PostMapper.from(request);

    FeedResponse feedResponse = feedService.feedFind(command, memberKey);

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

  //  @GetMapping("/feed/profileView/{memerKey}")
  //  public ResponseEntity<ApiResponse<FeedMyPageResponse>> mypage(
  //          UUID memberKey
  //          ,@Parameter(hidden = true) @AuthenticationPrincipal UUID key) {
  //
  //    FeedMyPageCommand command = PostMapper.from(memberKey);
  // FeedMyPageResponse response  =  feedService.memberProfileView(command);
  //
  //  }
  @Operation(summary = "북마크 추가/삭제", description = "true 반환 시 추가, false 반환 시 삭제  ")
  @PostMapping("/feed/bookmark/{postId}")
  public ResponseEntity<ApiResponse<UpdateBookMarked>> bookMark(
      @PathVariable Long postId,
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {

    UpdateBookMarked response = feedService.bookmarked(postId, memberKey);
    ApiResponse<UpdateBookMarked> result = ApiResponse.success(response);

    return ResponseEntity.ok().body(result);
  }

  @Operation(summary = "좋아요 추가/삭제", description = "true 반환 시 추가, false 반환 시 삭제  ")
  @PostMapping("/feed/like/{postId}")
  public ResponseEntity<ApiResponse<UpdateLiked>> like(
      @PathVariable Long postId,
      @Parameter(hidden = true) @AuthenticationPrincipal UUID memberKey) {
    UpdateLiked response = feedService.updateLiked(postId, memberKey);
    ApiResponse<UpdateLiked> result = ApiResponse.success(response);

    return ResponseEntity.ok().body(result);
  }
}
