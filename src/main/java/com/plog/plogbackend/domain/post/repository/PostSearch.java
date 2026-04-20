package com.plog.plogbackend.domain.post.repository;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostSearch {
  private long memberId;
  private LocalDate startDate;
  private LocalDate endDate;
}
