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
import com.plog.plogbackend.domain.review.entity.PlaceReviewImage;
import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewListItem;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
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
class PlaceReviewQueryRepositoryTest {

  @Autowired private PlaceReviewQueryRepository placeReviewQueryRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private PlaceRepository placeRepository;
  @Autowired private PostRepository postRepository;
  @Autowired private PlaceReviewRepository placeReviewRepository;
  @Autowired private PlaceReviewImageRepository placeReviewImageRepository;
  @Autowired private EntityManager em;

  @Test
  @DisplayName("장소 리뷰 목록을 최신순 커서 페이징으로 조회하고 환경 점수와 이미지를 함께 반환한다")
  void findReviewPageByPlaceId_returnsReviewsWithDetailsByCursor() {
    Place place = placeRepository.save(Place.of("콤파일", "서울 마포구 잔다리로 73", 37.0, 127.0));
    Place otherPlace = placeRepository.save(Place.of("다른 장소", "서울시 강남구", 37.1, 127.1));

    PlaceReview firstReview =
        saveReview(place, "첫번째", 3, "첫번째 리뷰", List.of("https://storage/first.jpg"));
    PlaceReview secondReview =
        saveReview(
            place,
            "두번째",
            4,
            "두번째 리뷰",
            List.of("https://storage/second-1.jpg", "https://storage/second-2.jpg"));
    PlaceReview thirdReview =
        saveReview(place, "세번째", 5, "세번째 리뷰", List.of("https://storage/third.jpg"));
    saveReview(otherPlace, "다른장소", 5, "다른 장소 리뷰", List.of("https://storage/other.jpg"));
    PlaceReview deletedReview =
        saveReview(place, "삭제됨", 1, "삭제된 리뷰", List.of("https://storage/deleted.jpg"));
    deletedReview.delete();
    flushAndClear();

    Slice<PlaceReviewListItem> firstPage =
        placeReviewQueryRepository.findReviewPageByPlaceId(place.getId(), cursor(null, 2));

    assertThat(firstPage.isHasNext()).isTrue();
    assertThat(firstPage.getContent())
        .extracting(PlaceReviewListItem::reviewId)
        .containsExactly(thirdReview.getId(), secondReview.getId());

    PlaceReviewListItem second = firstPage.getContent().get(1);
    assertThat(second.nickname()).isEqualTo("두번째");
    assertThat(second.rating()).isEqualTo(4);
    assertThat(second.content()).isEqualTo("두번째 리뷰");
    assertThat(second.environments()).containsEntry(ReviewEnvironmentName.SPACE_SIZE, 4);
    assertThat(second.imageUrls())
        .containsExactly("https://storage/second-1.jpg", "https://storage/second-2.jpg");

    String nextCursor = second.createdAt() + "|" + second.reviewId();
    Slice<PlaceReviewListItem> secondPage =
        placeReviewQueryRepository.findReviewPageByPlaceId(place.getId(), cursor(nextCursor, 2));

    assertThat(secondPage.isHasNext()).isFalse();
    assertThat(secondPage.getContent())
        .extracting(PlaceReviewListItem::reviewId)
        .containsExactly(firstReview.getId());
  }

  private PlaceReview saveReview(
      Place place, String nickname, int rating, String content, List<String> imageUrls) {
    Member member =
        memberRepository.save(
            Member.createNewMember(
                nickname, "provider-" + nickname, "https://profile/" + nickname, null));
    Post post = postRepository.save(createPost(member, place));
    PlaceReview review =
        placeReviewRepository.save(
            PlaceReview.create(post, member, rating, content, environments(rating)));
    imageUrls.forEach(
        imageUrl -> placeReviewImageRepository.save(PlaceReviewImage.of(review, imageUrl)));
    return review;
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

  private Cursorable<String> cursor(String cursor, int limit) {
    return new Cursorable<>(cursor, limit);
  }

  private void flushAndClear() {
    em.flush();
    em.clear();
  }
}
