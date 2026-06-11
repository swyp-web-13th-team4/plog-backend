package com.plog.plogbackend.domain.notification.service;

import com.plog.plogbackend.domain.notification.event.SseConnectedEvent;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // SSE 연결 타임아웃 시간 : 60분
  private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

  private final ApplicationEventPublisher eventPublisher;

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

    // 비동기 스레드에서 첫 연결 메시지 전송 후 구독 완료 이벤트 발행
    java.util.concurrent.CompletableFuture.runAsync(() -> {
          try {
            // 브라우저가 완전히 연결을 확립하고 준비할 수 있도록 150ms 딜레이를 줍니다.
            Thread.sleep(150);

            // 연결 확립 후 첫 데이터를 보내야 503 에러나 버퍼링 충돌을 막을 수 있음
            emitter.send(
                SseEmitter.event().name("connect").data("SSE 연결 성공 [memberId: " + memberId + "]"));

            // SSE 연결 완료 이벤트 발행 → 각 도메인이 미전송 알림을 재전송
            eventPublisher.publishEvent(new SseConnectedEvent(memberId));

          } catch (Exception e) {
            log.error("SSE connection error for memberId: {}", memberId, e);
            emitters.remove(memberId);
          }
        });

    return emitter;
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
