package com.plog.plogbackend.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.domain.review.entity.PlaceReviewImage;
import com.plog.plogbackend.domain.review.repository.PlaceReviewImageRepository;
import com.plog.plogbackend.domain.review.repository.PlaceReviewRepository;
import com.plog.plogbackend.global.util.GcsService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class PlaceReviewImageServiceTest {

  @Mock private GcsService gcsService;
  @Mock private PlaceReviewRepository placeReviewRepository;
  @Mock private PlaceReviewImageRepository placeReviewImageRepository;
  @Mock private PlaceReview placeReview;
  @InjectMocks private PlaceReviewImageService placeReviewImageService;

  @Test
  @DisplayName("리뷰 이미지가 없으면 업로드하지 않고 빈 목록을 반환한다")
  void uploadPlaceReviewImages_withoutImages() {
    List<ImageUrlResponse> responses = placeReviewImageService.uploadPlaceReviewImages(1L, null);

    assertThat(responses).isEmpty();
    verifyNoInteractions(gcsService, placeReviewRepository, placeReviewImageRepository);
  }

  @Test
  @DisplayName("리뷰 이미지를 업로드하고 이미지 URL 응답을 반환한다")
  void uploadPlaceReviewImages() {
    Long reviewId = 1L;
    List<MultipartFile> images =
        List.of(
            new MockMultipartFile("images", "a.jpg", "image/jpeg", "a".getBytes()),
            new MockMultipartFile("images", "b.jpg", "image/jpeg", "b".getBytes()));
    given(placeReviewRepository.findById(reviewId)).willReturn(Optional.of(placeReview));
    given(gcsService.upload(any(MultipartFile.class), eq("place-reviews")))
        .willReturn("https://storage/review-a.jpg", "https://storage/review-b.jpg");

    List<ImageUrlResponse> responses =
        placeReviewImageService.uploadPlaceReviewImages(reviewId, images);

    assertThat(responses)
        .extracting(ImageUrlResponse::imageUrl)
        .containsExactly("https://storage/review-a.jpg", "https://storage/review-b.jpg");
    verify(placeReviewImageRepository, times(2)).save(any(PlaceReviewImage.class));
    verify(placeReviewRepository).findById(reviewId);
    verify(gcsService, never()).delete(any());
  }
}
