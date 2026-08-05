package com.plog.plogbackend.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewEnvironmentCount;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewRatingSummary;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlaceReviewStatisticsRepositoryTest {

  @Autowired private PlaceReviewStatisticsRepository placeReviewStatisticsRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private PlaceRepository placeRepository;
  @Autowired private PostRepository postRepository;
  @Autowired private PlaceReviewRepository placeReviewRepository;
  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("삭제된 리뷰는 장소 리뷰 수와 평균 평점에 포함하지 않는다")
  void findRatingSummaryByPlaceId_excludesDeletedReviews() {
    Place place = savePlace();
    saveReview(place, "활성", 5, 5);
    PlaceReview deletedReview = saveReview(place, "삭제", 1, 1);
    deletedReview.delete();
    flushAndClear();

    PlaceReviewRatingSummary summary =
        placeReviewStatisticsRepository.findRatingSummaryByPlaceId(place.getId());

    assertThat(summary.reviewCount()).isEqualTo(1L);
    assertThat(summary.averageRating()).isEqualTo(5.0);
  }

  @Test
  @DisplayName("삭제된 리뷰의 환경 점수는 장소 환경 통계에 포함하지 않는다")
  void findEnvironmentCountsByPlaceId_excludesDeletedReviews() {
    Place place = savePlace();
    saveReview(place, "활성", 5, 5);
    PlaceReview deletedReview = saveReview(place, "삭제", 1, 1);
    deletedReview.delete();
    flushAndClear();

    List<PlaceReviewEnvironmentCount> counts =
        placeReviewStatisticsRepository.findEnvironmentCountsByPlaceId(place.getId());

    assertThat(counts)
        .containsExactlyInAnyOrder(
            new PlaceReviewEnvironmentCount(ReviewEnvironmentName.SPACE_SIZE, 5, 1L),
            new PlaceReviewEnvironmentCount(ReviewEnvironmentName.NOISE_LEVEL, 5, 1L),
            new PlaceReviewEnvironmentCount(ReviewEnvironmentName.CONGESTION_LEVEL, 5, 1L),
            new PlaceReviewEnvironmentCount(ReviewEnvironmentName.FOCUS_LEVEL, 5, 1L));
  }

  private Place savePlace() {
    return placeRepository.save(Place.of("콤파일", "서울 마포구 잔다리로 73", 37.0, 127.0));
  }

  private PlaceReview saveReview(Place place, String nickname, int rating, int environmentScore) {
    Member member =
        memberRepository.save(
            Member.createNewMember(
                nickname, "provider-" + nickname, "https://profile/" + nickname, null));
    Post post =
        postRepository.save(
            Post.of(
                "스터디 기록",
                "좋은 공간",
                LocalDateTime.of(2026, 1, 1, 9, 0),
                LocalDateTime.of(2026, 1, 1, 11, 0),
                LocalDate.of(2026, 1, 1),
                5,
                PublicScope.PUBLIC,
                member,
                place,
                PlaceCategoryCode.CAFE));
    return placeReviewRepository.save(
        PlaceReview.create(post, member, rating, "리뷰", environments(environmentScore)));
  }

  private Map<ReviewEnvironmentName, Integer> environments(int score) {
    Map<ReviewEnvironmentName, Integer> environments = new EnumMap<>(ReviewEnvironmentName.class);
    environments.put(ReviewEnvironmentName.SPACE_SIZE, score);
    environments.put(ReviewEnvironmentName.NOISE_LEVEL, score);
    environments.put(ReviewEnvironmentName.CONGESTION_LEVEL, score);
    environments.put(ReviewEnvironmentName.FOCUS_LEVEL, score);
    return environments;
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }
}
