package com.plog.plogbackend.domain.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class NotificationService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 60분
  private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

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
    }

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

  public void notify(Long memberId, Object data, String eventName) {
    SseEmitter emitter = emitters.get(memberId);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().name(eventName).data(data));
        log.info("Notification sent to memberId: {}, eventName: {}", memberId, eventName);
      } catch (IOException e) {
        log.error("Failed to send notification to memberId: {}", memberId, e);
        emitters.remove(memberId);
      }
    }
  }
}
