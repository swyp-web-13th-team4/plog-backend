package com.plog.plogbackend.domain.notification.event;

/**
 * SSE 구독이 완료되었을 때 발행되는 도메인 이벤트.
 *
 * <p>각 도메인(뱃지, 알림창 등)은 이 이벤트를 구독하여 구독 시점에 전송되지 않은 알림을 재전송할 수 있습니다.
 *
 * @param memberId SSE 연결을 맺은 회원의 내부 ID (PK)
 */
public record SseConnectedEvent(Long memberId) {}
