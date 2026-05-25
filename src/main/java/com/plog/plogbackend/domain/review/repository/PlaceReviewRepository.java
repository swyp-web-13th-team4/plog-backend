package com.plog.plogbackend.domain.review.repository;

import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.global.common.Enum.EntityStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlaceReviewRepository extends JpaRepository<PlaceReview, Long> {

  boolean existsByPostId(Long postId);

  Optional<PlaceReview> findByPostId(Long postId);

  Optional<PlaceReview> findByIdAndStatus(Long id, EntityStatus status);

  @Modifying
  @Query(
      """
      DELETE FROM PlaceReviewEnvironment environment
      WHERE environment.placeReview.id IN (
        SELECT review.id
        FROM PlaceReview review
        WHERE review.post.id = :postId
      )
      """)
  void deleteEnvironmentsByPostId(Long postId);

  @Modifying
  @Query("DELETE FROM PlaceReview review WHERE review.post.id = :postId")
  void deleteByPostId(Long postId);
}
