package com.plog.plogbackend.domain.notice.controller;

import com.plog.plogbackend.domain.notice.controller.dto.NewNoticeRequest;
import com.plog.plogbackend.domain.notice.controller.dto.NoticeListResponse;
import com.plog.plogbackend.domain.notice.controller.dto.NoticeRequest;
import com.plog.plogbackend.domain.notice.controller.dto.NoticeResponse;
import com.plog.plogbackend.domain.notice.service.NoticeService;
import com.plog.plogbackend.domain.notice.service.dto.NoticeResult;
import com.plog.plogbackend.domain.notice.service.dto.NoticesResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
public class AdminNoticeController {

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

    NoticeResult noticeResult = noticeService.findNotice(id);

    NoticeResponse response = NoticeResponse.of(noticeResult);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/new")
  public ResponseEntity<Long> newNotice(NewNoticeRequest request) {

    Long id = noticeService.create(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(id);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Long> updateNotice(@PathVariable Long id, NewNoticeRequest request) {
    noticeService.updateNotice(id, request);

    return ResponseEntity.ok(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {

    noticeService.delete(id);
    return ResponseEntity.ok(null);
  }
}
