package com.plog.plogbackend.domain.badge.event;

/**
 * 뱃지 부여를 요청하는 도메인 이벤트.
 *
 * <p>메인 트랜잭션이 커밋된 이후에 {@link BadgeEventHandler}가 수신하여 처리합니다.
 *
 * @param memberId 뱃지를 받을 회원의 내부 ID (PK)
 * @param badgeId 부여할 뱃지의 ID
 */
public record BadgeGrantEvent(Long memberId, Long badgeId) {}
