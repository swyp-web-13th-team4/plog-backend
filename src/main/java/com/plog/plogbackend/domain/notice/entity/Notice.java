package com.plog.plogbackend.domain.notice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Notice {

  @Id @GeneratedValue private Long id;

  private String title;
  private String content;
  private LocalDateTime localDateTime;

  @Builder
  private Notice(String title, String content) {
    this.title = title;
    this.content = content;
    this.localDateTime = LocalDateTime.now();
  }

  public void update(String title, String content) {
    this.title = title;
    this.content = content;
  }
}
