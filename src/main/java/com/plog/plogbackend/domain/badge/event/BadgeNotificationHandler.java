package com.plog.plogbackend.domain.badge.event;

import com.plog.plogbackend.domain.badge.dto.BadgeResponse;
import com.plog.plogbackend.domain.badge.entity.MemberBadge;
import com.plog.plogbackend.domain.badge.repository.MemberBadgeRepository;
import com.plog.plogbackend.domain.notification.event.SseConnectedEvent;
import com.plog.plogbackend.domain.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * SSE 구독 완료 이벤트를 수신하여 미전송 뱃지 알림을 재전송하는 핸들러.
 *
 * <p>{@link SseConnectedEvent}가 발행되면 해당 회원의 {@code notified = false} 뱃지 목록을 조회하여 SSE로 재전송하고 {@code
 * notified = true}로 마킹합니다.
 *
 * <p>별도의 트랜잭션({@code REQUIRES_NEW})으로 실행되므로, 재전송 중 오류가 발생해도 SSE 연결 흐름에는 영향을 주지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BadgeNotificationHandler {

  private final MemberBadgeRepository memberBadgeRepository;
  private final NotificationService notificationService;

  /**
   * SSE 연결 완료 이벤트 수신 시, 미전송 뱃지 알림을 일괄 재전송합니다.
   *
   * <p>회원가입 직후 SSE 연결이 없어 전송 실패한 첫 로그인 뱃지 등을 처리합니다. 전송 성공한 항목은 {@code notified = true}로 마킹합니다.
   */
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onSseConnected(SseConnectedEvent event) {
    Long memberId = event.memberId();

    List<MemberBadge> unnotified = memberBadgeRepository.findUnnotifiedByMemberId(memberId);
    if (unnotified.isEmpty()) {
      return;
    }

    log.info("미전송 뱃지 {} 건 재전송 시작 - memberId: {}", unnotified.size(), memberId);

    for (MemberBadge mb : unnotified) {
      BadgeResponse payload = BadgeResponse.from(mb.getBadge(), mb.getAcquiredAt());
      boolean sent = notificationService.notify(memberId, payload, "badge_grant");
      if (sent) {
        mb.markNotified();
        log.info("미전송 뱃지 재전송 성공 - memberId: {}, badgeId: {}", memberId, mb.getBadge().getId());
      } else {
        log.warn(
            "미전송 뱃지 재전송 실패 (emitter 없음) - memberId: {}, badgeId: {}",
            memberId,
            mb.getBadge().getId());
      }
    }
  }
}
