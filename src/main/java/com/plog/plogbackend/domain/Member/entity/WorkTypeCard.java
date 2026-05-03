package com.plog.plogbackend.domain.Member.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자의 작업 유형 카드를 나타내는 엔티티.
 *
 * <p>총 6가지 유형이 존재하며, SQL INSERT 문을 통해 초기 데이터를 주입합니다.
 *
 * <ul>
 *   <li>1. 성실 루틴형 치치 (시간의 규칙성)
 *   <li>2. 부지런한 아침형 로기 (오전 집중형)
 *   <li>3. 자유로운 탐험형 하루 (장소 유목민형)
 *   <li>4. 빠른 스퍼트형 토리 (단기 몰입형)
 *   <li>5. 고요한 새벽형 포포 (올빼미형)
 *   <li>6. 섬세한 예민형 나오 (환경 루틴형)
 * </ul>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkTypeCard {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String imageUrl;

  private String name;

  private String description;
}
