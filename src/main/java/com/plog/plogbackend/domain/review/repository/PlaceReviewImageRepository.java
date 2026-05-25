package com.plog.plogbackend.domain.review.repository;

import com.plog.plogbackend.domain.review.entity.PlaceReviewImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlaceReviewImageRepository extends JpaRepository<PlaceReviewImage, Long> {
  List<PlaceReviewImage> findAllByPlaceReviewId(Long placeReviewId);

  @Modifying
  @Query(
      """
      DELETE FROM PlaceReviewImage image
      WHERE image.placeReview.id IN (
        SELECT review.id
        FROM PlaceReview review
        WHERE review.post.id = :postId
      )
      """)
  void deleteAllByPostId(Long postId);
}
