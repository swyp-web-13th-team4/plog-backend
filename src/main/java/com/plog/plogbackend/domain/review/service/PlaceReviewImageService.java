package com.plog.plogbackend.domain.review.service;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.domain.review.entity.PlaceReviewImage;
import com.plog.plogbackend.domain.review.repository.PlaceReviewImageRepository;
import com.plog.plogbackend.domain.review.repository.PlaceReviewRepository;
import com.plog.plogbackend.global.common.Enum.EntityStatus;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.storage.CloudStorageService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PlaceReviewImageService {

  private static final String PLACE_REVIEW_DIR = "place-reviews";
  private static final int PLACE_REVIEW_IMAGE_MAX = 5;

  private final CloudStorageService cloudStorageService;
  private final PlaceReviewRepository placeReviewRepository;
  private final PlaceReviewImageRepository placeReviewImageRepository;

  public List<ImageUrlResponse> uploadPlaceReviewImages(
      Long placeReviewId, List<MultipartFile> images) {
    if (images == null || images.isEmpty()) {
      return List.of();
    }

    validateImages(images);

    PlaceReview placeReview = findActivePlaceReview(placeReviewId);

    List<String> uploadedUrls = new ArrayList<>();
    List<ImageUrlResponse> responses = new ArrayList<>();

    try {
      for (MultipartFile image : images) {
        String imageUrl = cloudStorageService.upload(image, PLACE_REVIEW_DIR);
        uploadedUrls.add(imageUrl);
        placeReviewImageRepository.save(PlaceReviewImage.of(placeReview, imageUrl));
        responses.add(new ImageUrlResponse(imageUrl));
      }
      return responses;
    } catch (RuntimeException e) {
      uploadedUrls.forEach(cloudStorageService::delete);
      throw e;
    }
  }

  public List<ImageUrlResponse> replacePlaceReviewImages(
      Long placeReviewId, List<Long> keepImageIds, List<MultipartFile> newImages) {
    PlaceReview placeReview = findActivePlaceReview(placeReviewId);

    List<Long> safeKeepIds = keepImageIds == null ? List.of() : keepImageIds;
    List<MultipartFile> safeNewImages =
        newImages == null
            ? List.of()
            : newImages.stream().filter(image -> image != null && !image.isEmpty()).toList();

    validateTotalImageCount(safeKeepIds.size(), safeNewImages.size());

    List<PlaceReviewImage> currentImages =
        placeReviewImageRepository.findAllByPlaceReviewId(placeReviewId);
    Set<Long> currentImageIds =
        currentImages.stream().map(PlaceReviewImage::getId).collect(Collectors.toSet());

    if (!currentImageIds.containsAll(safeKeepIds)) {
      throw new AppException(ErrorType.INVALID_ACCESS_PATH);
    }

    Set<Long> keepImageIdSet = new HashSet<>(safeKeepIds);
    List<ImageUrlResponse> responses = new ArrayList<>();
    List<PlaceReviewImage> deleteImages = new ArrayList<>();

    for (PlaceReviewImage image : currentImages) {
      if (keepImageIdSet.contains(image.getId())) {
        responses.add(new ImageUrlResponse(image.getImageUrl()));
        continue;
      }

      deleteImages.add(image);
    }

    List<String> uploadedUrls = new ArrayList<>();
    List<String> deleteUrls = deleteImages.stream().map(PlaceReviewImage::getImageUrl).toList();
    try {
      for (MultipartFile image : safeNewImages) {
        String imageUrl = cloudStorageService.upload(image, PLACE_REVIEW_DIR);
        uploadedUrls.add(imageUrl);
        placeReviewImageRepository.save(PlaceReviewImage.of(placeReview, imageUrl));
        responses.add(new ImageUrlResponse(imageUrl));
      }

      deleteImages.forEach(placeReviewImageRepository::delete);
      registerGcsCleanup(uploadedUrls, deleteUrls);
      return responses;
    } catch (RuntimeException e) {
      uploadedUrls.forEach(cloudStorageService::delete);
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

  private void validateTotalImageCount(int keepImageCount, int newImageCount) {
    if (keepImageCount + newImageCount > PLACE_REVIEW_IMAGE_MAX) {
      throw new AppException(ErrorType.PLACE_REVIEW_IMAGE_LIMIT_EXCEEDED);
    }
  }

  private PlaceReview findActivePlaceReview(Long placeReviewId) {
    return placeReviewRepository
        .findByIdAndStatus(placeReviewId, EntityStatus.ACTIVE)
        .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND));
  }

  private void registerGcsCleanup(List<String> uploadedUrls, List<String> deleteUrls) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      deleteUrls.forEach(cloudStorageService::delete);
      return;
    }

    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            deleteUrls.forEach(cloudStorageService::delete);
          }

          @Override
          public void afterCompletion(int status) {
            if (status == STATUS_ROLLED_BACK) {
              uploadedUrls.forEach(cloudStorageService::delete);
            }
          }
        });
  }

  @Transactional(readOnly = true)
  public List<ImageUrlResponse> getPlaceReviewImages(Long placeReviewId) {
    return placeReviewImageRepository.findAllByPlaceReviewId(placeReviewId).stream()
        .map(image -> new ImageUrlResponse(image.getImageUrl()))
        .toList();
  }
}
