package com.plog.plogbackend.domain.post.entity;

import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class PlaceCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true)
  private PlaceCategoryCode categoryName;
}
