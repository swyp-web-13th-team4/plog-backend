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
import com.plog.plogbackend.global.common.Enum.EntityStatus;
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
import org.springframework.test.util.ReflectionTestUtils;
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
    given(placeReviewRepository.findByIdAndStatus(reviewId, EntityStatus.ACTIVE))
        .willReturn(Optional.of(placeReview));
    given(gcsService.upload(any(MultipartFile.class), eq("place-reviews")))
        .willReturn("https://storage/review-a.jpg", "https://storage/review-b.jpg");

    List<ImageUrlResponse> responses =
        placeReviewImageService.uploadPlaceReviewImages(reviewId, images);

    assertThat(responses)
        .extracting(ImageUrlResponse::imageUrl)
        .containsExactly("https://storage/review-a.jpg", "https://storage/review-b.jpg");
    verify(placeReviewImageRepository, times(2)).save(any(PlaceReviewImage.class));
    verify(placeReviewRepository).findByIdAndStatus(reviewId, EntityStatus.ACTIVE);
    verify(gcsService, never()).delete(any());
  }

  @Test
  @DisplayName("리뷰 이미지를 keep + new 조합으로 교체한다")
  void replacePlaceReviewImages() {
    Long reviewId = 1L;
    PlaceReviewImage keepImage = PlaceReviewImage.of(placeReview, "https://storage/keep.jpg");
    PlaceReviewImage deleteImage = PlaceReviewImage.of(placeReview, "https://storage/delete.jpg");
    ReflectionTestUtils.setField(keepImage, "id", 10L);
    ReflectionTestUtils.setField(deleteImage, "id", 20L);
    List<MultipartFile> newImages =
        List.of(new MockMultipartFile("images", "new.jpg", "image/jpeg", "new".getBytes()));
    given(placeReviewRepository.findByIdAndStatus(reviewId, EntityStatus.ACTIVE))
        .willReturn(Optional.of(placeReview));
    given(placeReviewImageRepository.findAllByPlaceReviewId(reviewId))
        .willReturn(List.of(keepImage, deleteImage));
    given(gcsService.upload(any(MultipartFile.class), eq("place-reviews")))
        .willReturn("https://storage/new.jpg");

    List<ImageUrlResponse> responses =
        placeReviewImageService.replacePlaceReviewImages(reviewId, List.of(10L), newImages);

    assertThat(responses)
        .extracting(ImageUrlResponse::imageUrl)
        .containsExactly("https://storage/keep.jpg", "https://storage/new.jpg");
    verify(gcsService).delete("https://storage/delete.jpg");
    verify(placeReviewImageRepository).delete(deleteImage);
    verify(placeReviewImageRepository).save(any(PlaceReviewImage.class));
  }

  @Test
  @DisplayName("신규 이미지 업로드 실패 시 기존 GCS 이미지는 삭제하지 않고 업로드된 신규 이미지만 정리한다")
  void replacePlaceReviewImages_whenUploadFails_keepsExistingGcsImages() {
    Long reviewId = 1L;
    PlaceReviewImage deleteImage = PlaceReviewImage.of(placeReview, "https://storage/delete.jpg");
    ReflectionTestUtils.setField(deleteImage, "id", 20L);
    List<MultipartFile> newImages =
        List.of(
            new MockMultipartFile("images", "new-a.jpg", "image/jpeg", "new-a".getBytes()),
            new MockMultipartFile("images", "new-b.jpg", "image/jpeg", "new-b".getBytes()));
    given(placeReviewRepository.findByIdAndStatus(reviewId, EntityStatus.ACTIVE))
        .willReturn(Optional.of(placeReview));
    given(placeReviewImageRepository.findAllByPlaceReviewId(reviewId))
        .willReturn(List.of(deleteImage));
    given(gcsService.upload(any(MultipartFile.class), eq("place-reviews")))
        .willReturn("https://storage/new-a.jpg")
        .willThrow(new RuntimeException("upload failed"));

    org.assertj.core.api.Assertions.assertThatThrownBy(
            () -> placeReviewImageService.replacePlaceReviewImages(reviewId, List.of(), newImages))
        .isInstanceOf(RuntimeException.class);

    verify(gcsService, never()).delete("https://storage/delete.jpg");
    verify(gcsService).delete("https://storage/new-a.jpg");
    verify(placeReviewImageRepository, never()).delete(deleteImage);
  }
}
