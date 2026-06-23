package com.plog.plogbackend.global.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE(Server-Sent Events) 연결을 관리하는 글로벌 인프라 서비스.
 *
 * <p>이 클래스는 순수한 SSE 전송 인프라만 담당합니다. 알림 비즈니스 로직(DB 저장, 설정 필터링 등)은 {@code NotificationService}에서 처리하며,
 * 이 서비스는 최종 전송 수단으로만 사용됩니다.
 *
 * <p>멀티 디바이스/탭을 지원하며, 동일 유저가 여러 브라우저 탭에서 SSE를 구독해도 모든 연결에 이벤트가 전달됩니다.
 *
 * <h3>새로운 SSE 이벤트 종류를 추가하는 방법</h3>
 *
 * <p>이 서비스는 이벤트 이름(eventName)에 대해 어떠한 제약도 두지 않으므로, 어떤 도메인에서든 자유롭게 새로운 종류의 SSE 이벤트를 보낼 수 있습니다.
 *
 * <ol>
 *   <li>이 서비스를 주입받습니다.
 *       <pre>{@code
 * private final SseEmitterService sseEmitterService;
 *
 * }</pre>
 *   <li>{@link #notify(Long, Object, String)} 메서드를 호출합니다. 세 번째 파라미터 {@code eventName}이 프론트엔드의
 *       {@code EventSource.addEventListener}에 대응됩니다.
 *       <pre>{@code
 * // 예시 1: 채팅 메시지 실시간 전송
 * sseEmitterService.notify(memberId, chatMessageDto, "chat_message");
 *
 * // 예시 2: 실시간 피드 업데이트
 * sseEmitterService.notify(memberId, feedUpdateDto, "feed_update");
 *
 * // 예시 3: 뱃지 획득 알림 (현재 BadgeEventHandler에서 사용 중)
 * sseEmitterService.notify(memberId, badgeResponse, "badge_grant");
 *
 * // 예시 4: 알림창 알림 (현재 NotificationService에서 사용 중)
 * sseEmitterService.notify(memberId, notificationResponse, "notification");
 *
 * }</pre>
 *   <li>프론트엔드에서 해당 이벤트를 수신합니다.
 *       <pre>{@code
 * eventSource.addEventListener("chat_message", (event) => {
 *     const data = JSON.parse(event.data);
 *     // 수신된 데이터 처리
 * });
 *
 * }</pre>
 * </ol>
 *
 * <h4>현재 사용 중인 이벤트 이름 목록</h4>
 *
 * <table>
 *   <tr><th>eventName</th><th>발행 주체</th><th>설명</th></tr>
 *   <tr><td>{@code connect}</td><td>SseEmitterService (내부)</td><td>SSE 연결 성공 확인 메시지</td></tr>
 *   <tr><td>{@code heartbeat}</td><td>SseEmitterService (내부)</td><td>30초 주기 연결 유지 확인</td></tr>
 *   <tr><td>{@code notification}</td><td>NotificationService</td><td>알림창 알림 (좋아요, 문의답변, 신고 등)</td></tr>
 *   <tr><td>{@code badge_grant}</td><td>BadgeEventHandler</td><td>뱃지 획득 일회성 알림</td></tr>
 * </table>
 *
 * <p>※ 새 이벤트를 추가한 경우, 위 표에 행을 추가해 주세요.
 */
@Slf4j
@Service
public class SseEmitterService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // SSE 연결 타임아웃 시간 : 60분

  // [멀티 디바이스 지원] Map<Long, List<SseEmitter>> : 동일 유저의 다중 디바이스/탭 연결 지원
  private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

  // [전용 스레드 풀] ForkJoinPool.commonPool() 고갈 방지
  private final ExecutorService sseExecutor = Executors.newFixedThreadPool(10);

  private final ApplicationEventPublisher eventPublisher;

  public SseEmitterService(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  // ══════════════════════════════════════════════════════════════════════════════
  // SSE 연결 관리
  // ══════════════════════════════════════════════════════════════════════════════

  /**
   * 해당 memberId로 SSE 연결을 생성합니다.
   *
   * <p>연결 완료 후 {@link SseConnectedEvent}를 발행하여 각 도메인이 미전송 데이터를 재전송할 수 있도록 합니다.
   */
  public SseEmitter subscribe(Long memberId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

    // 해당 memberId의 리스트가 없으면 생성 후 emitter 추가 (원자적 처리)
    emitters.computeIfAbsent(memberId, id -> new CopyOnWriteArrayList<>()).add(emitter);

    // onCompletion / onTimeout / onError 세 가지 종료 케이스 모두 처리
    emitter.onCompletion(
        () -> {
          log.info("SSE connection completed for memberId: {}", memberId);
          removeEmitter(memberId, emitter);
        });
    emitter.onTimeout(
        () -> {
          log.info("SSE connection timeout for memberId: {}", memberId);
          removeEmitter(memberId, emitter);
        });
    emitter.onError(
        (e) -> {
          log.error("SSE connection error for memberId: {}", memberId, e);
          removeEmitter(memberId, emitter);
        });

    // 전용 sseExecutor 사용하여 첫 연결 메시지 전송
    sseExecutor.execute(
        () -> {
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
   */
  private void removeEmitter(Long memberId, SseEmitter emitter) {
    emitters.compute(
        memberId,
        (id, list) -> {
          if (list == null) return null;
          list.remove(emitter);
          return list.isEmpty() ? null : list;
        });
  }

  /** 30초마다 하트비트 전송. 연결 유지를 확인하고 클라이언트에게 연결 상태를 알림 */
  @Scheduled(fixedDelay = 30000)
  public void sendHeartbeat() {
    emitters.forEach(
        (memberId, emitterList) -> {
          for (SseEmitter emitter : emitterList) {
            try {
              sendToClient(emitter, SseEmitter.event().name("heartbeat").data("SSE 연결 유지 확인 중..."));
            } catch (IOException e) {
              log.info("Heartbeat 전송 실패로 인한 연결 제거 - memberId: {}", memberId);
              removeEmitter(memberId, emitter);
            }
          }
        });
  }

  /**
   * SSE 이벤트를 전송합니다. 해당 memberId로 연결된 모든 디바이스/탭에 전송합니다.
   *
   * @param memberId 수신자 회원 ID
   * @param data 전송할 데이터 (JSON 직렬화됨)
   * @param eventName SSE 이벤트 이름 (클라이언트의 EventSource.addEventListener에 대응)
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
        log.info("SSE event sent to memberId: {}, eventName: {}", memberId, eventName);
        anySuccess = true;
      } catch (IOException e) {
        log.error("Failed to send SSE event to memberId: {}", memberId, e);
        removeEmitter(memberId, emitter);
      }
    }
    return anySuccess;
  }

  /** SseEmitter.send()는 Thread-Safe하지 않으므로 emitter 인스턴스를 락 객체로 하여 synchronized로 직렬화합니다. */
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
