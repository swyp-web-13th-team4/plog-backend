package com.plog.plogbackend.domain.notification.controller;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.notification.dto.NotificationResponse;
import com.plog.plogbackend.domain.notification.dto.NotificationSettingResponse;
import com.plog.plogbackend.domain.notification.dto.NotificationSettingUpdateRequest;
import com.plog.plogbackend.domain.notification.service.NotificationService;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.response.ApiResponse;
import com.plog.plogbackend.global.sse.SseEmitterService;
import com.plog.plogbackend.global.support.paging.CursorDefault;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "알림")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

  private final NotificationService notificationService;
  private final SseEmitterService sseEmitterService;
  private final MemberRepository memberRepository;

  // ══════════════════════════════════════════════════════════════════════════════
  // SSE 구독
  // ══════════════════════════════════════════════════════════════════════════════

  @Operation(summary = "SSE 알림 구독", description = "로그인한 사용자의 SSE 연결을 생성합니다.")
  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(
      @AuthenticationPrincipal UUID memberKey, jakarta.servlet.http.HttpServletResponse response) {

    // Nginx 버퍼링 강제 비활성화
    response.setHeader("X-Accel-Buffering", "no");
    response.setHeader("Cache-Control", "no-cache");

    Member member = findMemberByKey(memberKey);
    return sseEmitterService.subscribe(member.getId());
  }

  // ══════════════════════════════════════════════════════════════════════════════
  // 알림 목록 조회
  // ══════════════════════════════════════════════════════════════════════════════

  @Operation(summary = "알림 목록 조회", description = "커서 기반 페이지네이션으로 알림 목록을 최신순으로 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse>> getNotifications(
      @AuthenticationPrincipal UUID memberKey,
      @CursorDefault(defaultLimit = 20) Cursorable<String> cursorable) {

    Member member = findMemberByKey(memberKey);
    Slice<NotificationResponse> slice =
        notificationService.getNotifications(member.getId(), cursorable);

    return ResponseEntity.ok(ApiResponse.success(PageResponse.of(slice)));
  }

  @Operation(summary = "안 읽은 알림 개수 조회", description = "읽지 않은 알림의 개수를 반환합니다.")
  @GetMapping("/unread-count")
  public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
      @AuthenticationPrincipal UUID memberKey) {

    Member member = findMemberByKey(memberKey);
    long count = notificationService.getUnreadCount(member.getId());

    return ResponseEntity.ok(ApiResponse.success(new UnreadCountResponse(count)));
  }

  // ══════════════════════════════════════════════════════════════════════════════
  // 알림 읽음 처리
  // ══════════════════════════════════════════════════════════════════════════════

  @Operation(summary = "알림 단건 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
  @PatchMapping("/{id}/read")
  public ResponseEntity<ApiResponse<Void>> markAsRead(
      @AuthenticationPrincipal UUID memberKey, @PathVariable Long id) {

    Member member = findMemberByKey(memberKey);
    notificationService.markAsRead(member.getId(), id);

    return ResponseEntity.ok(ApiResponse.success());
  }

  @Operation(summary = "알림 전체 읽음 처리", description = "모든 알림을 읽음 상태로 변경합니다.")
  @PatchMapping("/read-all")
  public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal UUID memberKey) {

    Member member = findMemberByKey(memberKey);
    notificationService.markAllAsRead(member.getId());

    return ResponseEntity.ok(ApiResponse.success());
  }

  // ══════════════════════════════════════════════════════════════════════════════
  // 알림 삭제 (Hard Delete)
  // ══════════════════════════════════════════════════════════════════════════════

  @Operation(summary = "알림 단건 삭제", description = "특정 알림을 영구 삭제합니다.")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNotification(
      @AuthenticationPrincipal UUID memberKey, @PathVariable Long id) {

    Member member = findMemberByKey(memberKey);
    notificationService.deleteNotification(member.getId(), id);

    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "알림 전체 삭제", description = "모든 알림을 영구 삭제합니다.")
  @DeleteMapping("/all")
  public ResponseEntity<Void> deleteAllNotifications(@AuthenticationPrincipal UUID memberKey) {

    Member member = findMemberByKey(memberKey);
    notificationService.deleteAllNotifications(member.getId());

    return ResponseEntity.noContent().build();
  }

  // ══════════════════════════════════════════════════════════════════════════════
  // 알림 설정
  // ══════════════════════════════════════════════════════════════════════════════

  @Operation(summary = "알림 설정 조회", description = "전체 알림 ON/OFF 및 타입별 설정을 조회합니다.")
  @GetMapping("/settings")
  public ResponseEntity<ApiResponse<NotificationSettingResponse>> getSettings(
      @AuthenticationPrincipal UUID memberKey) {

    Member member = findMemberByKey(memberKey);
    NotificationSettingResponse settings = notificationService.getSettings(member.getId());

    return ResponseEntity.ok(ApiResponse.success(settings));
  }

  @Operation(summary = "알림 설정 변경", description = "전체 알림 ON/OFF 또는 타입별 설정을 변경합니다.")
  @PatchMapping("/settings")
  public ResponseEntity<ApiResponse<Void>> updateSettings(
      @AuthenticationPrincipal UUID memberKey,
      @RequestBody NotificationSettingUpdateRequest request) {

    Member member = findMemberByKey(memberKey);
    notificationService.updateSettings(member, request);

    return ResponseEntity.ok(ApiResponse.success());
  }

  // ══════════════════════════════════════════════════════════════════════════════
  // 내부 유틸
  // ══════════════════════════════════════════════════════════════════════════════

  private Member findMemberByKey(UUID memberKey) {
    return memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));
  }

  // ── 응답용 inner record ──────────────────────────────────────────────────────

  /** 알림 목록 페이지 응답. 기존 프로젝트의 PageResponse 패턴을 따릅니다. */
  record PageResponse(
      java.util.List<NotificationResponse> content, boolean hasNext, String nextCursor) {
    static PageResponse of(Slice<NotificationResponse> slice) {
      return new PageResponse(slice.getContent(), slice.isHasNext(), slice.getNextCursor());
    }
  }

  /** 안 읽은 알림 개수 응답 */
  record UnreadCountResponse(long unreadCount) {}
}
