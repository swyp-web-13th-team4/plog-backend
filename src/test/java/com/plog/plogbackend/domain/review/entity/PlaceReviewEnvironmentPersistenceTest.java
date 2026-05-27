package com.plog.plogbackend.domain.review.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.repository.PlaceReviewRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
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
class PlaceReviewEnvironmentPersistenceTest {

  @Autowired private PlaceReviewRepository placeReviewRepository;
  @Autowired private PostRepository postRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private PlaceRepository placeRepository;
  @Autowired private EntityManager em;

  @Test
  @DisplayName("장소 리뷰의 환경 점수는 PlaceReviewEnvironment 엔티티로 저장된다")
  void savePlaceReview_persistsEnvironmentEntities() {
    LocalDateTime beforeCreate = LocalDateTime.now();
    Member member =
        memberRepository.save(Member.createNewMember("reviewer", "provider-reviewer", null, null));
    Place place = placeRepository.save(Place.of("테스트 카페", "서울시 강남구", 37.0, 127.0));
    Post post = postRepository.save(createPost(member, place));
    Map<ReviewEnvironmentName, Integer> environments = new EnumMap<>(ReviewEnvironmentName.class);
    environments.put(ReviewEnvironmentName.SPACE_SIZE, 5);
    environments.put(ReviewEnvironmentName.NOISE_LEVEL, 4);
    environments.put(ReviewEnvironmentName.CONGESTION_LEVEL, 3);
    environments.put(ReviewEnvironmentName.FOCUS_LEVEL, 2);

    PlaceReview review =
        placeReviewRepository.save(PlaceReview.create(post, member, 5, "집중하기 좋았어요", environments));
    em.flush();
    LocalDateTime afterCreate = LocalDateTime.now();
    em.clear();

    Long environmentCount =
        em.createQuery(
                "select count(environment) from PlaceReviewEnvironment environment "
                    + "where environment.placeReview.id = :reviewId",
                Long.class)
            .setParameter("reviewId", review.getId())
            .getSingleResult();
    PlaceReview foundReview = placeReviewRepository.findById(review.getId()).orElseThrow();

    assertThat(environmentCount).isEqualTo(4L);
    assertThat(foundReview.getEnvironments()).containsAllEntriesOf(environments);
    assertThat(foundReview.getEditableUntil())
        .isBetween(beforeCreate.plusDays(30), afterCreate.plusDays(30));
  }

  @Test
  @DisplayName("장소 리뷰 환경 점수 수정 시 기존 환경 엔티티를 중복 삽입하지 않고 갱신한다")
  void updatePlaceReview_replacesEnvironmentEntitiesWithoutUniqueConstraintViolation() {
    Member member =
        memberRepository.save(Member.createNewMember("reviewer", "provider-reviewer", null, null));
    Place place = placeRepository.save(Place.of("테스트 카페", "서울시 강남구", 37.0, 127.0));
    Post post = postRepository.save(createPost(member, place));
    PlaceReview review =
        placeReviewRepository.save(
            PlaceReview.create(post, member, 5, "집중하기 좋았어요", environments(5)));
    em.flush();

    review.update(3, "수정했어요", environments(3), LocalDateTime.now());
    em.flush();
    em.clear();

    PlaceReview updatedReview = placeReviewRepository.findById(review.getId()).orElseThrow();

    assertThat(updatedReview.getRating()).isEqualTo(3);
    assertThat(updatedReview.getContent()).isEqualTo("수정했어요");
    assertThat(updatedReview.getEnvironments()).containsAllEntriesOf(environments(3));
  }

  private Post createPost(Member member, Place place) {
    return Post.of(
        "스터디 기록",
        "좋은 공간",
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 1, 1, 11, 0),
        LocalDate.of(2026, 1, 1),
        5,
        PublicScope.PUBLIC,
        member,
        place,
        PlaceCategoryCode.CAFE);
  }

  private Map<ReviewEnvironmentName, Integer> environments(int score) {
    Map<ReviewEnvironmentName, Integer> environments = new EnumMap<>(ReviewEnvironmentName.class);
    environments.put(ReviewEnvironmentName.SPACE_SIZE, score);
    environments.put(ReviewEnvironmentName.NOISE_LEVEL, score);
    environments.put(ReviewEnvironmentName.CONGESTION_LEVEL, score);
    environments.put(ReviewEnvironmentName.FOCUS_LEVEL, score);
    return environments;
  }
}
