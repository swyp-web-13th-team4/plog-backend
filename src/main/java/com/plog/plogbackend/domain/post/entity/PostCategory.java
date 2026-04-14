package com.plog.plogbackend.domain.post.entity;

import jakarta.persistence.*;

@Entity
public class PostCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_category_id")
  private PlaceCategory placeCategory;
}
