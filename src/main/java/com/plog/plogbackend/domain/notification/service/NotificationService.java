package com.plog.plogbackend.domain.notification.service;

import com.plog.plogbackend.domain.badge.dto.BadgeResponse;
import com.plog.plogbackend.domain.badge.entity.MemberBadge;
import com.plog.plogbackend.domain.badge.repository.MemberBadgeRepository;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 60분
  private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

  private final MemberBadgeRepository memberBadgeRepository;

  public SseEmitter subscribe(Long memberId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

    emitters.put(memberId, emitter);

    emitter.onCompletion(
        () -> {
          log.info("SSE connection completed for memberId: {}", memberId);
          emitters.remove(memberId);
        });
    emitter.onTimeout(
        () -> {
          log.info("SSE connection timeout for memberId: {}", memberId);
          emitters.remove(memberId);
        });

    // SSE 연결 시 첫 데이터를 보내지 않으면 503 에러가 발생할 수 있음
    try {
      emitter.send(
          SseEmitter.event().name("connect").data("SSE 연결 성공 [memberId: " + memberId + "]"));
    } catch (IOException e) {
      log.error("SSE connection error for memberId: {}", memberId, e);
      emitters.remove(memberId);
      return emitter;
    }

    // 구독 시점에 미전송 뱃지 알림을 일괄 전송
    flushUnnotifiedBadges(memberId);

    return emitter;
  }

  /**
   * SSE 구독 시점에 아직 전송되지 않은 뱃지 알림을 일괄 전송합니다.
   *
   * <p>회원가입 직후 SSE 연결이 없어 전송 실패한 첫 로그인 뱃지 등을 처리합니다.
   * 전송 성공한 항목은 {@code notified = true}로 마킹합니다.
   */
  @Transactional
  public void flushUnnotifiedBadges(Long memberId) {
    List<MemberBadge> unnotified = memberBadgeRepository.findUnnotifiedByMemberId(memberId);
    if (unnotified.isEmpty()) {
      return;
    }

    log.info("미전송 뱃지 {} 건 재전송 시작 - memberId: {}", unnotified.size(), memberId);

    for (MemberBadge mb : unnotified) {
      BadgeResponse payload = BadgeResponse.from(mb.getBadge(), mb.getAcquiredAt());
      boolean sent = notify(memberId, payload, "badge_grant");
      if (sent) {
        mb.markNotified();
        log.info(
            "미전송 뱃지 재전송 성공 - memberId: {}, badgeId: {}", memberId, mb.getBadge().getId());
      } else {
        log.warn(
            "미전송 뱃지 재전송 실패 (emitter 없음) - memberId: {}, badgeId: {}",
            memberId,
            mb.getBadge().getId());
      }
    }
  }

  /** 30초마다 하트비트 전송 연결 유지를 확인하고 클라이언트에게 연결 상태를 알림 */
  @Scheduled(fixedDelay = 30000)
  public void sendHeartbeat() {
    emitters.forEach(
        (memberId, emitter) -> {
          try {
            emitter.send(SseEmitter.event().name("heartbeat").data("SSE 연결 유지 확인 중..."));
          } catch (IOException e) {
            log.info("Heartbeat 전송 실패로 인한 연결 제거 - memberId: {}", memberId);
            emitters.remove(memberId);
          }
        });
  }

  /**
   * SSE 알림을 전송합니다.
   *
   * @return 전송 성공 여부 (emitter가 없거나 IOException 발생 시 false)
   */
  public boolean notify(Long memberId, Object data, String eventName) {
    SseEmitter emitter = emitters.get(memberId);
    if (emitter == null) {
      return false;
    }
    try {
      emitter.send(SseEmitter.event().name(eventName).data(data));
      log.info("Notification sent to memberId: {}, eventName: {}", memberId, eventName);
      return true;
    } catch (IOException e) {
      log.error("Failed to send notification to memberId: {}", memberId, e);
      emitters.remove(memberId);
      return false;
    }
  }
}
