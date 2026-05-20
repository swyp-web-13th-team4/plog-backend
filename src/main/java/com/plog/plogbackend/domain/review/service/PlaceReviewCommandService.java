package com.plog.plogbackend.domain.review.service;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewResponse;
import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewCreateCommand;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewDeleteCommand;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewUpdateCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PlaceReviewCommandService {

  private final PlaceReviewService placeReviewService;
  private final PlaceReviewImageService placeReviewImageService;

  @Transactional
  public PlaceReviewResponse create(PlaceReviewCreateCommand command, List<MultipartFile> images) {
    placeReviewImageService.validateImages(images);

    PlaceReview placeReview = placeReviewService.create(command);

    List<ImageUrlResponse> uploadedImages =
        placeReviewImageService.replacePlaceReviewImages(placeReview.getId(), List.of(), images);

    return PlaceReviewResponse.from(placeReview, uploadedImages);
  }

  @Transactional
  public PlaceReviewResponse update(PlaceReviewUpdateCommand command, List<MultipartFile> images) {
    PlaceReview placeReview = placeReviewService.update(command);
    List<ImageUrlResponse> imageResponses =
        placeReviewImageService.replacePlaceReviewImages(
            placeReview.getId(), command.keepImageIds(), images);
    return PlaceReviewResponse.from(placeReview, imageResponses);
  }

  @Transactional
  public void delete(PlaceReviewDeleteCommand command) {
    placeReviewService.delete(command);
  }
}
