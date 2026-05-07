package com.plog.plogbackend.domain.post.entity;

import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자 추가
public class PlaceCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true)
  private PlaceCategoryCode categoryName;

  @Builder
  public PlaceCategory(PlaceCategoryCode categoryName) {
    this.categoryName = categoryName;
  }
}
