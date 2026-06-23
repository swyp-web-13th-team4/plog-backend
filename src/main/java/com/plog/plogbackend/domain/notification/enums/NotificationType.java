package com.plog.plogbackend.domain.notification.enums;

/**
 * 알림창에 표시되는 알림의 종류를 정의합니다.
 *
 * <p>※ 뱃지(BADGE) 획득 알림은 알림창 저장 대상이 아닙니다. 뱃지 알림은 기존과 동일하게 SSE로만 일회성으로 전송되며 이 Enum과 무관합니다. ({@code
 * BadgeEventHandler} 참조)
 *
 * <h3>새로운 알림 종류를 추가하는 방법</h3>
 *
 * <ol>
 *   <li>이 Enum에 새로운 상수를 추가합니다. (예: {@code MENTION})
 *   <li>알림을 발생시키는 도메인 로직(예: CommentService)에서 {@code NotificationService.sendNotification(member,
 *       NotificationType.MENTION, ...)}을 호출합니다.
 *   <li>끝! DB 스키마 변경이나 알림 설정 관련 코드 수정은 필요하지 않습니다. {@code NotificationTypeSetting} 테이블이 (member_id,
 *       notification_type) 조합으로 자동 관리되기 때문입니다.
 * </ol>
 */
public enum NotificationType {

  /** 내 게시글에 좋아요를 눌렀을 때 */
  POST_LIKE,

  /** 문의에 대한 답변이 등록되었을 때 */
  INQUIRY_ANSWER,

  /** 기타 신고 처리 결과 알림 */
  REPORT
}
