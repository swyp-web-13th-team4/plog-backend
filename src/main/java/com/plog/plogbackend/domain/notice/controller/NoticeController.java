package com.plog.plogbackend.domain.notice.controller;

import com.plog.plogbackend.domain.notice.controller.dto.NoticeListResponse;
import com.plog.plogbackend.domain.notice.controller.dto.NoticeRequest;
import com.plog.plogbackend.domain.notice.controller.dto.NoticeResponse;
import com.plog.plogbackend.domain.notice.service.NoticeService;
import com.plog.plogbackend.domain.notice.service.dto.NoticeResult;
import com.plog.plogbackend.domain.notice.service.dto.NoticesResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

  private final NoticeService noticeService;

  @GetMapping
  public ResponseEntity<List<NoticeListResponse>> noticeList(NoticeRequest request) {

    List<NoticesResult> noticeResults = noticeService.noticeResultList(request);

    List<NoticeListResponse> responses =
        noticeResults.stream().map(NoticeListResponse::of).toList();

    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<NoticeResponse> notice(@PathVariable Long id) {

    NoticeResult noticeResult = noticeService.noticeResult(id);

    NoticeResponse response = NoticeResponse.of(noticeResult);

    return ResponseEntity.ok(response);
  }
}
