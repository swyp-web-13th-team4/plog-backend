package com.plog.plogbackend.domain.review.service;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.domain.review.entity.PlaceReviewImage;
import com.plog.plogbackend.domain.review.repository.PlaceReviewImageRepository;
import com.plog.plogbackend.domain.review.repository.PlaceReviewRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.util.GcsService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PlaceReviewImageService {

  private static final String PLACE_REVIEW_DIR = "place-reviews";
  private static final int PLACE_REVIEW_IMAGE_MAX = 5;

  private final GcsService gcsService;
  private final PlaceReviewRepository placeReviewRepository;
  private final PlaceReviewImageRepository placeReviewImageRepository;

  public List<ImageUrlResponse> uploadPlaceReviewImages(
      Long placeReviewId, List<MultipartFile> images) {
    if (images == null || images.isEmpty()) {
      return List.of();
    }

    validateImages(images);

    PlaceReview placeReview =
        placeReviewRepository
            .findById(placeReviewId)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND));

    List<String> uploadedUrls = new ArrayList<>();
    List<ImageUrlResponse> responses = new ArrayList<>();

    try {
      for (MultipartFile image : images) {
        String imageUrl = gcsService.upload(image, PLACE_REVIEW_DIR);
        uploadedUrls.add(imageUrl);
        placeReviewImageRepository.save(PlaceReviewImage.of(placeReview, imageUrl));
        responses.add(new ImageUrlResponse(imageUrl));
      }
      return responses;
    } catch (RuntimeException e) {
      uploadedUrls.forEach(gcsService::delete);
      throw e;
    }
  }

  public void validateImages(List<MultipartFile> images) {
    if (images == null || images.isEmpty()) {
      return;
    }

    if (images.size() > PLACE_REVIEW_IMAGE_MAX) {
      throw new AppException(ErrorType.PLACE_REVIEW_IMAGE_LIMIT_EXCEEDED);
    }
  }

  @Transactional(readOnly = true)
  public List<ImageUrlResponse> getPlaceReviewImages(Long placeReviewId) {
    return placeReviewImageRepository.findAllByPlaceReviewId(placeReviewId).stream()
        .map(image -> new ImageUrlResponse(image.getImageUrl()))
        .toList();
  }
}
