package com.plog.plogbackend.domain.badge.event;

import com.plog.plogbackend.domain.badge.dto.BadgeResponse;
import com.plog.plogbackend.domain.badge.entity.Badge;
import com.plog.plogbackend.domain.badge.entity.MemberBadge;
import com.plog.plogbackend.domain.badge.repository.BadgeRepository;
import com.plog.plogbackend.domain.badge.repository.MemberBadgeRepository;
import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.global.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 뱃지 부여 이벤트 핸들러.
 *
 * <ul>
 *   <li>{@code phase = AFTER_COMMIT}: 메인 트랜잭션이 성공적으로 커밋된 이후에만 실행됩니다.
 *   <li>{@code REQUIRES_NEW}: 뱃지 부여를 위한 독립적인 새 트랜잭션을 시작합니다. 뱃지 저장 중 오류가 발생해도 이미 커밋된 메인 로직에는 영향을 주지
 *       않습니다.
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BadgeEventHandler {

  private final MemberRepository memberRepository;
  private final BadgeRepository badgeRepository;
  private final MemberBadgeRepository memberBadgeRepository;
  private final SseEmitterService sseEmitterService;

  /**
   * {@link BadgeGrantEvent}를 수신하여 뱃지를 부여합니다.
   *
   * <p>메인 트랜잭션 커밋 후 별도의 새 트랜잭션에서 실행되며, 예외가 발생해도 메인 로직의 데이터는 보호됩니다.
   *
   * <p>SSE 연결이 아직 없으면 {@code notified = false}로 저장하고, SSE 구독 시점에 {@link
   * NotificationService#flushUnnotifiedBadges}가 재전송합니다.
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleBadgeGrant(BadgeGrantEvent event) {
    try {
      // 이미 보유한 뱃지면 스킵
      if (memberBadgeRepository.existsByMemberIdAndBadgeId(event.memberId(), event.badgeId())) {
        log.debug("뱃지 중복 지급 스킵 - memberId: {}, badgeId: {}", event.memberId(), event.badgeId());
        return;
      }

      Member member =
          memberRepository
              .findById(event.memberId())
              .orElseThrow(
                  () ->
                      new IllegalStateException("뱃지 부여 실패 - 회원 없음: memberId=" + event.memberId()));

      Badge badge =
          badgeRepository
              .findById(event.badgeId())
              .orElseThrow(
                  () -> new IllegalStateException("뱃지 부여 실패 - 뱃지 없음: badgeId=" + event.badgeId()));

      // notified = false 로 저장 (SSE 전송 성공 후 true 로 변경)
      MemberBadge memberBadge = memberBadgeRepository.save(MemberBadge.of(member, badge));

      log.info(
          "뱃지 획득 - memberId: {}, Nickname: {}, badgeId: {}, badgeName: {}",
          member.getId(),
          member.getNickname(),
          badge.getId(),
          badge.getName());

      // SSE 알림 전송 시도
      // - 연결이 있으면 즉시 전송 후 notified = true 마킹
      // - 연결이 없으면 notified = false 유지 → 구독 시점에 flushUnnotifiedBadges가 재전송
      boolean sent =
          sseEmitterService.notify(
              member.getId(),
              BadgeResponse.from(badge, memberBadge.getAcquiredAt()),
              "badge_grant");

      if (sent) {
        memberBadge.markNotified();
        log.info("SSE 뱃지 알림 전송 완료 - memberId: {}, badgeId: {}", member.getId(), badge.getId());
      } else {
        log.info(
            "SSE 연결 없음, 뱃지 알림 보류 (구독 시 재전송 예정) - memberId: {}, badgeId: {}",
            member.getId(),
            badge.getId());
      }

    } catch (Exception e) {
      // 뱃지 오류는 메인 로직에 영향을 주지 않도록 로그만 남기고 삼킵니다.
      log.error(
          "뱃지 부여 중 오류 발생 (메인 로직에는 영향 없음) - memberId: {}, badgeId: {}, error: {}",
          event.memberId(),
          event.badgeId(),
          e.getMessage(),
          e);
    }
  }
}
