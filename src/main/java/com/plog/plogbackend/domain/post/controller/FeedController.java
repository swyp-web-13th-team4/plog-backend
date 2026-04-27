package com.plog.plogbackend.domain.post.controller;

import com.plog.plogbackend.domain.post.controller.api.PostMapper;
import com.plog.plogbackend.domain.post.controller.dto.request.FeedDetailRequest;
import com.plog.plogbackend.domain.post.controller.dto.request.FeedFindRequest;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedDetailResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.FeedFindResponse;
import com.plog.plogbackend.domain.post.service.FeedService;
import com.plog.plogbackend.domain.post.service.dto.FeedDetailCommand;
import com.plog.plogbackend.domain.post.service.dto.FeedFindCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FeedController {

  private final FeedService feedService;

  @GetMapping("/list")
  public ResponseEntity<List<FeedFindResponse>> feedList(FeedFindRequest request) {

    FeedFindCommand command = PostMapper.from(request);

    List<FeedFindResponse> feedFindResponses = feedService.feedFind(command);

    return ResponseEntity.ok().body(feedFindResponses);
  }

  @GetMapping("/list{id}")
  public ResponseEntity<FeedDetailResponse> feedDetail(FeedDetailRequest request) {

    FeedDetailCommand command = PostMapper.from(request);
    FeedDetailResponse response = feedService.feedDetail(command);

    return ResponseEntity.ok().body(response);
  }
}
