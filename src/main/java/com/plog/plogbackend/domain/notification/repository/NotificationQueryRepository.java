package com.plog.plogbackend.domain.notification.repository;

import com.plog.plogbackend.domain.notification.entity.Notification;
import com.plog.plogbackend.domain.notification.entity.QNotification;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 커서 기반 페이지네이션을 사용한 알림 조회 Repository.
 *
 * <p>기존 프로젝트의 {@code Cursorable}, {@code Slice} 인프라를 그대로 활용합니다. 커서 값으로 {@code notification.id}(단조
 * 증가 PK)를 사용하며, 최신순(역순) 탐색합니다.
 */
@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {

  private final JPAQueryFactory queryFactory;

  private static final QNotification notification = QNotification.notification;

  /**
   * 해당 사용자의 알림 목록을 커서 기반 페이지네이션으로 조회합니다.
   *
   * @param receiverId 수신자 ID
   * @param cursorable 커서 정보 (cursor: 마지막으로 본 알림 ID, limit: 페이지 크기)
   * @return 알림 Slice (content, hasNext, nextCursor 포함)
   */
  public Slice<Notification> findByReceiverId(Long receiverId, Cursorable<String> cursorable) {
    List<Notification> results =
        queryFactory
            .selectFrom(notification)
            .where(notification.receiver.id.eq(receiverId), cursorCondition(cursorable.getCursor()))
            .orderBy(notification.id.desc())
            .limit(cursorable.getLimit() + 1)
            .fetch();

    return Slice.of(results, cursorable, n -> String.valueOf(n.getId()));
  }

  /**
   * 커서 조건: notification.id < cursor (최신순 역순 탐색)
   *
   * <p>첫 페이지 요청 시(cursor == null)에는 조건 없이 최신부터 조회합니다.
   */
  private BooleanExpression cursorCondition(String cursor) {
    if (cursor == null || cursor.isBlank()) return null;
    try {
      return notification.id.lt(Long.parseLong(cursor));
    } catch (NumberFormatException e) {
      throw new com.plog.plogbackend.global.error.AppException(
          com.plog.plogbackend.global.error.ErrorType.INVALID_ACCESS_PATH);
    }
  }
}
