package com.plog.plogbackend.domain.review.entity;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "place_review_environment",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_place_review_environment_review_name",
            columnNames = {"review_id", "environment_name"}))
public class PlaceReviewEnvironment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private PlaceReview placeReview;

  @Enumerated(EnumType.STRING)
  @Column(name = "environment_name", nullable = false)
  private ReviewEnvironmentName name;

  @Column(name = "score", nullable = false)
  private Integer score;

  private PlaceReviewEnvironment(
      PlaceReview placeReview, ReviewEnvironmentName name, Integer score) {
    this.placeReview = placeReview;
    this.name = name;
    this.score = score;
  }

  static PlaceReviewEnvironment of(
      PlaceReview placeReview, ReviewEnvironmentName name, Integer score) {
    return new PlaceReviewEnvironment(placeReview, name, score);
  }
}
