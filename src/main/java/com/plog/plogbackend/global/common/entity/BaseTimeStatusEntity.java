package com.plog.plogbackend.global.common.entity;

import com.plog.plogbackend.global.common.enums.EntityStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseTimeStatusEntity extends BaseTimeEntity {

  private LocalDateTime deletedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EntityStatus status = EntityStatus.ACTIVE; // protected → private

  public void deleteEntity() {
    this.status = EntityStatus.DELETED;
    this.deletedAt = LocalDateTime.now();
  }
}
