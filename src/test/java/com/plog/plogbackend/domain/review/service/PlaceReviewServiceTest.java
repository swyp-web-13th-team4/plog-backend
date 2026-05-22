package com.plog.plogbackend.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.repository.PlaceReviewRepository;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewCreateCommand;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewDeleteCommand;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewUpdateCommand;
import com.plog.plogbackend.global.common.Enum.EntityStatus;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class PlaceReviewServiceTest {

  @Mock private MemberRepository memberRepository;
  @Mock private PostRepository postRepository;
  @Mock private PlaceReviewRepository placeReviewRepository;
  @Mock private Member member;
  @Mock private Post post;
  @InjectMocks private PlaceReviewService placeReviewService;

  @Test
  @DisplayName("삭제된 장소 리뷰가 있으면 새로 저장하지 않고 기존 리뷰를 복구해 덮어쓴다")
  void create_deletedReview_restoresExistingReview() {
    UUID memberKey = UUID.randomUUID();
    PlaceReviewCreateCommand command =
        new PlaceReviewCreateCommand(1L, memberKey, 3, "다시 작성", environments());
    PlaceReview deletedReview =
        PlaceReview.create(post, member, 5, "삭제된 리뷰", environments(5, 5, 5, 5));
    deletedReview.delete();
    given(member.getId()).willReturn(1L);
    given(post.getMember()).willReturn(member);
    given(memberRepository.findByMemberKey(memberKey)).willReturn(Optional.of(member));
    given(postRepository.findById(command.postId())).willReturn(Optional.of(post));
    given(placeReviewRepository.findByPostId(command.postId()))
        .willReturn(Optional.of(deletedReview));

    PlaceReview review = placeReviewService.create(command);

    assertThat(review).isSameAs(deletedReview);
    assertThat(review.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    assertThat(review.getDeletedAt()).isNull();
    assertThat(review.getRating()).isEqualTo(3);
    assertThat(review.getContent()).isEqualTo("다시 작성");
    assertThat(review.getEnvironments()).containsExactlyInAnyOrderEntriesOf(command.environments());
    verify(placeReviewRepository, never()).save(review);
  }

  @Test
  @DisplayName("활성 장소 리뷰가 이미 있으면 중복 작성 예외를 던진다")
  void create_activeReview_throwsAlreadyExists() {
    UUID memberKey = UUID.randomUUID();
    PlaceReviewCreateCommand command =
        new PlaceReviewCreateCommand(1L, memberKey, 3, "다시 작성", environments());
    PlaceReview activeReview = PlaceReview.create(post, member, 5, "기존 리뷰", environments());
    given(member.getId()).willReturn(1L);
    given(post.getMember()).willReturn(member);
    given(memberRepository.findByMemberKey(memberKey)).willReturn(Optional.of(member));
    given(postRepository.findById(command.postId())).willReturn(Optional.of(post));
    given(placeReviewRepository.findByPostId(command.postId()))
        .willReturn(Optional.of(activeReview));

    assertThatThrownBy(() -> placeReviewService.create(command))
        .isInstanceOf(AppException.class)
        .extracting("errorType")
        .isEqualTo(ErrorType.PLACE_REVIEW_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("신규 장소 리뷰 저장 중 unique 제약 조건이 충돌하면 중복 작성 예외로 변환한다")
  void create_uniqueConstraintViolation_throwsAlreadyExists() {
    UUID memberKey = UUID.randomUUID();
    PlaceReviewCreateCommand command =
        new PlaceReviewCreateCommand(1L, memberKey, 3, "좋았어요", environments());
    given(member.getId()).willReturn(1L);
    given(post.getMember()).willReturn(member);
    given(memberRepository.findByMemberKey(memberKey)).willReturn(Optional.of(member));
    given(postRepository.findById(command.postId())).willReturn(Optional.of(post));
    given(placeReviewRepository.findByPostId(command.postId())).willReturn(Optional.empty());
    given(placeReviewRepository.saveAndFlush(any(PlaceReview.class)))
        .willThrow(new DataIntegrityViolationException("uk_place_review_post"));

    assertThatThrownBy(() -> placeReviewService.create(command))
        .isInstanceOf(AppException.class)
        .extracting("errorType")
        .isEqualTo(ErrorType.PLACE_REVIEW_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("삭제된 장소 리뷰는 수정 대상에서 제외한다")
  void update_deletedReview_throwsNotFound() {
    PlaceReviewUpdateCommand command =
        new PlaceReviewUpdateCommand(1L, UUID.randomUUID(), 4, "수정", environments(), List.of());
    given(placeReviewRepository.findByIdAndStatus(command.reviewId(), EntityStatus.ACTIVE))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> placeReviewService.update(command))
        .isInstanceOf(AppException.class)
        .extracting("errorType")
        .isEqualTo(ErrorType.NOT_FOUND);

    verify(placeReviewRepository).findByIdAndStatus(command.reviewId(), EntityStatus.ACTIVE);
  }

  @Test
  @DisplayName("장소 리뷰 삭제 시 활성 리뷰를 삭제 상태로 변경한다")
  void delete_activeReview_marksReviewDeleted() {
    UUID memberKey = UUID.randomUUID();
    PlaceReviewDeleteCommand command = new PlaceReviewDeleteCommand(10L, memberKey);
    given(member.getMemberKey()).willReturn(memberKey);
    PlaceReview review = PlaceReview.create(post, member, 5, "좋았어요", environments());
    given(placeReviewRepository.findByIdAndStatus(command.reviewId(), EntityStatus.ACTIVE))
        .willReturn(Optional.of(review));

    placeReviewService.delete(command);

    assertThat(review.getStatus()).isEqualTo(EntityStatus.DELETED);
    assertThat(review.getDeletedAt()).isNotNull();
  }

  @Test
  @DisplayName("이미 삭제된 장소 리뷰는 삭제 대상에서 제외한다")
  void delete_deletedReview_throwsNotFound() {
    PlaceReviewDeleteCommand command = new PlaceReviewDeleteCommand(10L, UUID.randomUUID());
    given(placeReviewRepository.findByIdAndStatus(command.reviewId(), EntityStatus.ACTIVE))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> placeReviewService.delete(command))
        .isInstanceOf(AppException.class)
        .extracting("errorType")
        .isEqualTo(ErrorType.NOT_FOUND);
  }

  private Map<ReviewEnvironmentName, Integer> environments() {
    return environments(5, 4, 3, 2);
  }

  private Map<ReviewEnvironmentName, Integer> environments(
      int spaceSize, int noiseLevel, int congestionLevel, int focusLevel) {
    Map<ReviewEnvironmentName, Integer> environments = new EnumMap<>(ReviewEnvironmentName.class);
    environments.put(ReviewEnvironmentName.SPACE_SIZE, spaceSize);
    environments.put(ReviewEnvironmentName.NOISE_LEVEL, noiseLevel);
    environments.put(ReviewEnvironmentName.CONGESTION_LEVEL, congestionLevel);
    environments.put(ReviewEnvironmentName.FOCUS_LEVEL, focusLevel);
    return environments;
  }
}
