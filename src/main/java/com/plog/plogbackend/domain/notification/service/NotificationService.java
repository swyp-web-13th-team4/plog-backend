package com.plog.plogbackend.domain.notification.service;

import com.plog.plogbackend.domain.notification.event.SseConnectedEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

  // [포인트 1] Map<Long, List<SseEmitter>> : 동일 유저의 다중 디바이스/탭 연결 지원
  // CopyOnWriteArrayList: List 순회 중 다른 스레드의 add/remove로 인한 ConcurrentModificationException 방지
  private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

  // [포인트 4] 전용 스레드 풀 : ForkJoinPool.commonPool() 고갈 방지
  // newCachedThreadPool()은 스레드가 무한정 생성되는 위험이 있으므로 상한이 있는 FixedThreadPool 사용
  private final ExecutorService sseExecutor = Executors.newFixedThreadPool(10);

  private final ApplicationEventPublisher eventPublisher;

  public SseEmitter subscribe(Long memberId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

    // [포인트 1] 해당 memberId의 리스트가 없으면 생성 후 emitter 추가 (원자적 처리)
    emitters.computeIfAbsent(memberId, id -> new CopyOnWriteArrayList<>()).add(emitter);

    // [포인트 3] onCompletion / onTimeout / onError 세 가지 종료 케이스 모두 처리
    emitter.onCompletion(() -> {
      log.info("SSE connection completed for memberId: {}", memberId);
      removeEmitter(memberId, emitter);
    });
    emitter.onTimeout(() -> {
      log.info("SSE connection timeout for memberId: {}", memberId);
      removeEmitter(memberId, emitter);
    });
    // 기존 코드에 누락되어 있던 onError 콜백 - 비정상 종료 시 메모리 누수 방지
    emitter.onError((e) -> {
      log.error("SSE connection error for memberId: {}", memberId, e);
      removeEmitter(memberId, emitter);
    });

    // [포인트 4] 공용 ForkJoinPool 대신 전용 sseExecutor 사용
    sseExecutor.execute(() -> {
      try {
        // 브라우저가 완전히 연결을 확립하고 준비할 수 있도록 150ms 딜레이를 줍니다.
        Thread.sleep(150);

        // 연결 확립 후 첫 데이터를 보내야 503 에러나 버퍼링 충돌을 막을 수 있음
        sendToClient(
            emitter,
            SseEmitter.event().name("connect").data("SSE 연결 성공 [memberId: " + memberId + "]"));

        // SSE 연결 완료 이벤트 발행 → 각 도메인이 미전송 알림을 재전송
        eventPublisher.publishEvent(new SseConnectedEvent(memberId));

      } catch (Exception e) {
        log.error("SSE initial message error for memberId: {}", memberId, e);
        removeEmitter(memberId, emitter);
      }
    });

    return emitter;
  }

  /**
   * 특정 emitter를 해당 memberId 리스트에서 안전하게 제거합니다.
   *
   * <p>ConcurrentHashMap.compute()를 사용하여 리스트 수정과 Map 키 제거를 원자적으로 처리합니다.
   * 단순 list.isEmpty() 체크 후 remove(key)를 분리하면, 두 스레드가 동시에 마지막 emitter를
   * 제거할 때 새 구독자의 emitter가 의도치 않게 삭제되는 race condition이 발생할 수 있습니다.
   */
  private void removeEmitter(Long memberId, SseEmitter emitter) {
    emitters.compute(
        memberId,
        (id, list) -> {
          if (list == null) return null;
          list.remove(emitter);
          // 리스트가 비면 null을 반환 → compute()가 Map에서 해당 키를 원자적으로 제거
          return list.isEmpty() ? null : list;
        });
  }

  /** 30초마다 하트비트 전송 연결 유지를 확인하고 클라이언트에게 연결 상태를 알림 */
  @Scheduled(fixedDelay = 30000)
  public void sendHeartbeat() {
    emitters.forEach(
        (memberId, emitterList) -> {
          // CopyOnWriteArrayList의 iterator는 스냅샷 기반이므로 순회 중 remove가 발생해도 안전
          for (SseEmitter emitter : emitterList) {
            try {
              sendToClient(
                  emitter, SseEmitter.event().name("heartbeat").data("SSE 연결 유지 확인 중..."));
            } catch (IOException e) {
              log.info("Heartbeat 전송 실패로 인한 연결 제거 - memberId: {}", memberId);
              removeEmitter(memberId, emitter);
            }
          }
        });
  }

  /**
   * SSE 알림을 전송합니다. 해당 memberId로 연결된 모든 디바이스/탭에 전송합니다.
   *
   * @return 하나 이상의 emitter에 전송 성공 시 true, emitter가 없거나 전부 실패 시 false
   */
  public boolean notify(Long memberId, Object data, String eventName) {
    List<SseEmitter> emitterList = emitters.get(memberId);
    if (emitterList == null || emitterList.isEmpty()) {
      return false;
    }

    boolean anySuccess = false;
    for (SseEmitter emitter : emitterList) {
      try {
        sendToClient(emitter, SseEmitter.event().name(eventName).data(data));
        log.info("Notification sent to memberId: {}, eventName: {}", memberId, eventName);
        anySuccess = true;
      } catch (IOException e) {
        log.error("Failed to send notification to memberId: {}", memberId, e);
        removeEmitter(memberId, emitter);
      }
    }
    return anySuccess;
  }

  /**
   * [포인트 2] SseEmitter.send()는 Thread-Safe하지 않으므로 emitter 인스턴스를 락 객체로 하여
   * synchronized로 직렬화합니다. 하트비트 스케줄러와 notify()가 동시에 send()를 호출하는
   * 경우에도 IllegalStateException / 데이터 손상을 방지합니다.
   */
  private void sendToClient(SseEmitter emitter, SseEmitter.SseEventBuilder event)
      throws IOException {
    synchronized (emitter) {
      emitter.send(event);
    }
  }

  /** 애플리케이션 종료 시 전용 스레드 풀을 안전하게 정리합니다. */
  @jakarta.annotation.PreDestroy
  public void shutdown() {
    sseExecutor.shutdown();
    try {
      if (!sseExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        sseExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      sseExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
