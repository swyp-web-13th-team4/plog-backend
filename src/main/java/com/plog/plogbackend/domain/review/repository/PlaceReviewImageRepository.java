package com.plog.plogbackend.domain.review.repository;

import com.plog.plogbackend.domain.review.entity.PlaceReviewImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceReviewImageRepository extends JpaRepository<PlaceReviewImage, Long> {
  List<PlaceReviewImage> findAllByPlaceReviewId(Long placeReviewId);
}
