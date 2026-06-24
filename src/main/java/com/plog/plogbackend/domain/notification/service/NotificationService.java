package com.plog.plogbackend.domain.notification.service;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.notification.dto.NotificationResponse;
import com.plog.plogbackend.domain.notification.dto.NotificationSettingResponse;
import com.plog.plogbackend.domain.notification.dto.NotificationSettingUpdateRequest;
import com.plog.plogbackend.domain.notification.entity.Notification;
import com.plog.plogbackend.domain.notification.entity.NotificationSetting;
import com.plog.plogbackend.domain.notification.entity.NotificationTypeSetting;
import com.plog.plogbackend.domain.notification.enums.NotificationType;
import com.plog.plogbackend.domain.notification.repository.NotificationQueryRepository;
import com.plog.plogbackend.domain.notification.repository.NotificationRepository;
import com.plog.plogbackend.domain.notification.repository.NotificationSettingRepository;
import com.plog.plogbackend.domain.notification.repository.NotificationTypeSettingRepository;
import com.plog.plogbackend.global.sse.SseEmitterService;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림창 비즈니스 로직 서비스.
 *
 * <p>SSE 전송 인프라({@link SseEmitterService})와 분리되어 있으며, 이 클래스는 알림의 생성(DB 저장), 조회, 읽음 처리, 삭제, 설정 관리 등
 * 순수한 비즈니스 로직만 담당합니다.
 *
 * <h3>새로운 알림 종류를 추가하는 방법</h3>
 *
 * <ol>
 *   <li>{@link NotificationType} Enum에 새 상수를 추가합니다. (예: {@code MENTION})
 *   <li>알림을 발생시키는 도메인 서비스에서 이 클래스의 {@link #sendNotification}을 호출합니다.
 *       <pre>{@code
 * notificationService.sendNotification(
 *     receiver, NotificationType.MENTION,
 *     "OOO님이 댓글에서 당신을 언급했습니다.",
 *     "/post/123");
 *
 * }</pre>
 *   <li>끝! DB 스키마 변경 없이 자동으로 알림 설정 필터링, DB 저장, SSE 전송이 모두 처리됩니다.
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final SseEmitterService sseEmitterService;
  private final NotificationRepository notificationRepository;
  private final NotificationQueryRepository notificationQueryRepository;
  private final NotificationSettingRepository notificationSettingRepository;
  private final NotificationTypeSettingRepository notificationTypeSettingRepository;

  // ══════════════════════════════════════════════════════════════════════════════
  // 알림 생성 (DB 저장 + SSE 전송)
  // ══════════════════════════════════════════════════════════════════════════════

  /**
   * 알림을 생성합니다. DB에 저장한 뒤 실시간 SSE 전송을 시도합니다.
   *
   * <p>사용자의 알림 설정({@link NotificationSetting}, {@link NotificationTypeSetting})에 따라 알림이 차단될 수 있습니다.
   * 차단된 경우 DB에 저장되지 않으며 SSE도 전송되지 않습니다.
   *
   * <h3>새 알림 종류를 추가한 후 호출 예시</h3>
   *
   * <pre>{@code
   * notificationService.sendNotification(
   *     receiver, NotificationType.MENTION,
   *     senderNickname + "님이 댓글에서 당신을 언급했습니다.",
   *     "/post/" + postId);
   * }</pre>
   *
   * @param receiver 알림을 받을 사용자
   * @param type 알림 종류
   * @param content 알림 내용 텍스트
   * @param relatedUrl 클릭 시 이동할 URL
   */
  @Transactional
  public void sendNotification(
      Member receiver, NotificationType type, String content, String relatedUrl) {

    // 1. 전체 알림 설정 확인
    NotificationSetting setting =
        notificationSettingRepository.findByMemberId(receiver.getId()).orElse(null);
    if (setting != null && !setting.isAllEnabled()) {
      log.debug("알림 차단 (전체 OFF) - memberId: {}, type: {}", receiver.getId(), type);
      return;
    }

    // 2. 개별 타입 설정 확인
    NotificationTypeSetting typeSetting =
        notificationTypeSettingRepository
            .findByMemberIdAndNotificationType(receiver.getId(), type)
            .orElse(null);
    // typeSetting이 null이면 아직 설정한 적 없으므로 기본값(true)으로 취급
    if (typeSetting != null && !typeSetting.isEnabled()) {
      log.debug("알림 차단 (타입 OFF) - memberId: {}, type: {}", receiver.getId(), type);
      return;
    }

    // 3. DB 저장
    Notification notification = Notification.create(receiver, type, content, relatedUrl);
    notificationRepository.save(notification);

    // 4. SSE 실시간 전송 (글로벌 SseEmitterService에 위임)
    NotificationResponse response = NotificationResponse.from(notification);
    sseEmitterService.notify(receiver.getId(), response, "notification");
  }

  // ══════════════════════════════════════════════════════════════════════════════
  // 알림 조회
  // ══════════════════════════════════════════════════════════════════════════════

  /** 알림 목록을 커서 기반 페이지네이션으로 조회합니다. */
  @Transactional(readOnly = true)
  public Slice<NotificationResponse> getNotifications(
      Long memberId, Cursorable<String> cursorable) {
    return notificationQueryRepository
        .findByReceiverId(memberId, cursorable)
        .map(NotificationResponse::from);
  }

  /** 안 읽은 알림 개수를 조회합니다. */
  @Transactional(readOnly = true)
  public long getUnreadCount(Long memberId) {
    return notificationRepository.countByReceiverIdAndIsReadFalse(memberId);
  }

  // ══════════════════════════════════════════════════════════════════════════════
  // 알림 읽음 처리
  // ══════════════════════════════════════════════════════════════════════════════

  /** 특정 알림을 읽음 처리합니다. 소유권 검증 포함. */
  @Transactional
  public void markAsRead(Long memberId, Long notificationId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
    validateOwnership(memberId, notification);
    notification.markAsRead();
  }

  /** 해당 사용자의 모든 알림을 읽음 처리합니다. */
  @Transactional
  public void markAllAsRead(Long memberId) {
    notificationRepository.markAllAsRead(memberId);
  }

  // ══════════════════════════════════════════════════════════════════════════════
  // 알림 삭제 (Hard Delete)
  // ══════════════════════════════════════════════════════════════════════════════

  /** 특정 알림을 삭제합니다. 소유권 검증 포함. */
  @Transactional
  public void deleteNotification(Long memberId, Long notificationId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
    validateOwnership(memberId, notification);
    notificationRepository.delete(notification);
  }

  /** 해당 사용자의 모든 알림을 삭제합니다. */
  @Transactional
  public void deleteAllNotifications(Long memberId) {
    notificationRepository.deleteAllByReceiverId(memberId);
  }

  // ══════════════════════════════════════════════════════════════════════════════
  // 알림 설정
  // ══════════════════════════════════════════════════════════════════════════════

  /** 알림 설정을 조회합니다. 설정이 없으면 기본값(전체 ON, 모든 타입 ON)으로 응답합니다. */
  @Transactional(readOnly = true)
  public NotificationSettingResponse getSettings(Long memberId) {
    boolean isAllEnabled =
        notificationSettingRepository
            .findByMemberId(memberId)
            .map(NotificationSetting::isAllEnabled)
            .orElse(true);

    List<NotificationTypeSetting> typeSettings =
        notificationTypeSettingRepository.findAllByMemberId(memberId);

    // 저장된 타입 설정을 Map으로 변환
    Map<NotificationType, Boolean> savedMap =
        typeSettings.stream()
            .collect(
                Collectors.toMap(
                    NotificationTypeSetting::getNotificationType,
                    NotificationTypeSetting::isEnabled));

    // 모든 NotificationType에 대해 설정값 구성 (없으면 기본값 true)
    List<NotificationSettingResponse.TypeSettingEntry> entries =
        Arrays.stream(NotificationType.values())
            .map(
                type ->
                    new NotificationSettingResponse.TypeSettingEntry(
                        type, savedMap.getOrDefault(type, true)))
            .toList();

    return new NotificationSettingResponse(isAllEnabled, entries);
  }

  /** 알림 설정을 변경합니다. */
  @Transactional
  public void updateSettings(Member member, NotificationSettingUpdateRequest request) {

    // 전체 설정 업데이트
    if (request.isAllEnabled() != null) {
      NotificationSetting setting =
          notificationSettingRepository
              .findByMemberId(member.getId())
              .orElseGet(
                  () ->
                      notificationSettingRepository.save(
                          NotificationSetting.createDefault(member)));
      setting.updateAllEnabled(request.isAllEnabled());
    }

    // 개별 타입 설정 업데이트
    if (request.typeSettings() != null) {
      for (NotificationSettingUpdateRequest.TypeSettingEntry entry : request.typeSettings()) {
        NotificationTypeSetting typeSetting =
            notificationTypeSettingRepository
                .findByMemberIdAndNotificationType(member.getId(), entry.type())
                .orElseGet(
                    () ->
                        notificationTypeSettingRepository.save(
                            NotificationTypeSetting.createDefault(member, entry.type())));
        typeSetting.updateEnabled(entry.enabled());
      }
    }
  }

  // ══════════════════════════════════════════════════════════════════════════════
  // 소유권 검증
  // ══════════════════════════════════════════════════════════════════════════════

  /**
   * 해당 알림이 현재 로그인한 사용자의 것인지 검증합니다.
   *
   * @throws org.springframework.security.access.AccessDeniedException 타인의 알림에 접근할 경우
   */
  private void validateOwnership(Long memberId, Notification notification) {
    if (!notification.getReceiver().getId().equals(memberId)) {
      throw new org.springframework.security.access.AccessDeniedException("해당 알림에 대한 권한이 없습니다.");
    }
  }
}
