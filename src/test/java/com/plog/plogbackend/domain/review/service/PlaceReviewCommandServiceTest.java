package com.plog.plogbackend.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewResponse;
import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewCreateCommand;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewUpdateCommand;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class PlaceReviewCommandServiceTest {

  @Mock private PlaceReviewService placeReviewService;
  @Mock private PlaceReviewImageService placeReviewImageService;
  @Mock private PlaceReview placeReview;
  @Mock private Post post;
  @Mock private Place place;
  @InjectMocks private PlaceReviewCommandService placeReviewCommandService;

  @Test
  @DisplayName("이미지 검증 후 리뷰를 생성하고 업로드된 이미지와 함께 응답한다")
  void create_validatesImagesBeforeCreatingReviewAndUploadsImages() {
    PlaceReviewCreateCommand command =
        new PlaceReviewCreateCommand(1L, UUID.randomUUID(), 5, "좋았어요", environments());
    List<MultipartFile> images =
        List.of(new MockMultipartFile("images", "review.jpg", "image/jpeg", "review".getBytes()));
    List<ImageUrlResponse> imageResponses =
        List.of(new ImageUrlResponse("https://storage/review.jpg"));

    given(placeReviewService.create(command)).willReturn(placeReview);
    given(placeReview.getId()).willReturn(10L);
    given(placeReview.getPost()).willReturn(post);
    given(placeReview.getRating()).willReturn(5);
    given(placeReview.getEnvironments()).willReturn(command.environments());
    given(placeReview.getContent()).willReturn("좋았어요");
    given(post.getId()).willReturn(1L);
    given(post.getPlace()).willReturn(place);
    given(post.getStudyDate()).willReturn(LocalDate.of(2026, 1, 1));
    given(post.getStartedAt()).willReturn(LocalDateTime.of(2026, 1, 1, 9, 0));
    given(post.getEndedAt()).willReturn(LocalDateTime.of(2026, 1, 1, 11, 0));
    given(place.getId()).willReturn(2L);
    given(place.getName()).willReturn("테스트 카페");
    given(placeReviewImageService.uploadPlaceReviewImages(placeReview.getId(), images))
        .willReturn(imageResponses);

    PlaceReviewResponse response = placeReviewCommandService.create(command, images);

    assertThat(response.imageUrls()).containsExactly("https://storage/review.jpg");
    InOrder inOrder = inOrder(placeReviewImageService, placeReviewService);
    inOrder.verify(placeReviewImageService).validateImages(images);
    inOrder.verify(placeReviewService).create(command);
    inOrder.verify(placeReviewImageService).uploadPlaceReviewImages(placeReview.getId(), images);
  }

  @Test
  @DisplayName("리뷰 수정 후 기존 이미지와 함께 응답한다")
  void update_returnsUpdatedReviewWithImages() {
    PlaceReviewUpdateCommand command =
        new PlaceReviewUpdateCommand(10L, UUID.randomUUID(), 4, "수정했어요", environments());
    List<ImageUrlResponse> imageResponses =
        List.of(new ImageUrlResponse("https://storage/existing-review.jpg"));

    given(placeReviewService.update(command)).willReturn(placeReview);
    given(placeReview.getId()).willReturn(10L);
    given(placeReview.getPost()).willReturn(post);
    given(placeReview.getRating()).willReturn(4);
    given(placeReview.getEnvironments()).willReturn(command.environments());
    given(placeReview.getContent()).willReturn("수정했어요");
    given(post.getId()).willReturn(1L);
    given(post.getPlace()).willReturn(place);
    given(post.getStudyDate()).willReturn(LocalDate.of(2026, 1, 1));
    given(post.getStartedAt()).willReturn(LocalDateTime.of(2026, 1, 1, 9, 0));
    given(post.getEndedAt()).willReturn(LocalDateTime.of(2026, 1, 1, 11, 0));
    given(place.getId()).willReturn(2L);
    given(place.getName()).willReturn("테스트 카페");
    given(placeReviewImageService.getPlaceReviewImages(placeReview.getId()))
        .willReturn(imageResponses);

    PlaceReviewResponse response = placeReviewCommandService.update(command);

    assertThat(response.rating()).isEqualTo(4);
    assertThat(response.content()).isEqualTo("수정했어요");
    assertThat(response.imageUrls()).containsExactly("https://storage/existing-review.jpg");
  }

  private Map<ReviewEnvironmentName, Integer> environments() {
    Map<ReviewEnvironmentName, Integer> environments = new EnumMap<>(ReviewEnvironmentName.class);
    environments.put(ReviewEnvironmentName.SPACE_SIZE, 5);
    environments.put(ReviewEnvironmentName.NOISE_LEVEL, 4);
    environments.put(ReviewEnvironmentName.CONGESTION_LEVEL, 3);
    environments.put(ReviewEnvironmentName.FOCUS_LEVEL, 2);
    return environments;
  }
}
