package com.plog.plogbackend.global.common.entity;

import com.plog.plogbackend.global.common.Enum.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeStatusEntity {

  @CreatedDate
  @Column(updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  // 삭제 시각 기록 (삭제되지 않았을 때는 null)
  private LocalDateTime deletedAt;

  // 공통 상태 관리 (기본값 ACTIVE)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  protected Status status = Status.ACTIVE;

  /** 논리적 삭제 */
  public void deleteEntity() {
    this.status = Status.DELETED;
    this.deletedAt = LocalDateTime.now();
  }
}
