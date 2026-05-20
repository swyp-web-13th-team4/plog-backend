package com.plog.plogbackend.domain.review.repository;

import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.global.common.Enum.EntityStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceReviewRepository extends JpaRepository<PlaceReview, Long> {

  boolean existsByPostId(Long postId);

  Optional<PlaceReview> findByPostId(Long postId);

  Optional<PlaceReview> findByIdAndStatus(Long id, EntityStatus status);
}
