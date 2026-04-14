package com.plog.plogbackend.domain.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Member {
  // TODO : 영속성 객체 (DB 모델링 필요)

  @Id @GeneratedValue private Long id;
}
