package com.plog.plogbackend.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

  @Builder
  private PostCategory(Post post, PlaceCategory placeCategory) {
    this.post = post;
    this.placeCategory = placeCategory;
  }

  public static PostCategory of(Post post, PlaceCategory placeCategory) {
    return PostCategory.builder()
        .post(post)
        .placeCategory(placeCategory)
        .build();
  }
}
