package com.plog.plogbackend.domain.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceReviewImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_review_id", nullable = false)
  private PlaceReview placeReview;

  @Column(nullable = false)
  private String imageUrl;

  private PlaceReviewImage(PlaceReview placeReview, String imageUrl) {
    this.placeReview = placeReview;
    this.imageUrl = imageUrl;
  }

  public static PlaceReviewImage of(PlaceReview placeReview, String imageUrl) {
    return new PlaceReviewImage(placeReview, imageUrl);
  }
}
