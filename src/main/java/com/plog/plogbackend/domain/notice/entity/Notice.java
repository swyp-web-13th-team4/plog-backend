package com.plog.plogbackend.domain.notice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Getter
public class Notice {

  @Id @GeneratedValue private Long id;

  private String title;
  private String content;

  private LocalDateTime localDateTime;
}
