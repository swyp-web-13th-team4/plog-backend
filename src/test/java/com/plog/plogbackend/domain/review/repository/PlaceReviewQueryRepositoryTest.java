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
import com.plog.plogbackend.domain.review.enums.PlaceReviewSortType;
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
        placeReviewQueryRepository.findReviewPageByPlaceId(
            place.getId(), cursor(null, 2), false, PlaceReviewSortType.LATEST);

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
        placeReviewQueryRepository.findReviewPageByPlaceId(
            place.getId(), cursor(nextCursor, 2), false, PlaceReviewSortType.LATEST);

    assertThat(secondPage.isHasNext()).isFalse();
    assertThat(secondPage.getContent())
        .extracting(PlaceReviewListItem::reviewId)
        .containsExactly(firstReview.getId());
  }

  @Test
  @DisplayName("사진 리뷰만 조회하면 이미지가 있는 리뷰만 반환한다")
  void findReviewPageByPlaceId_whenImageOnly_returnsOnlyReviewsWithImages() {
    Place place = placeRepository.save(Place.of("콤파일", "서울 마포구 잔다리로 73", 37.0, 127.0));

    PlaceReview firstReview =
        saveReview(place, "첫번째", 3, "첫번째 리뷰", List.of("https://storage/first.jpg"));
    saveReview(place, "사진없음", 4, "사진 없는 리뷰", List.of());
    PlaceReview thirdReview =
        saveReview(
            place,
            "세번째",
            5,
            "세번째 리뷰",
            List.of("https://storage/third-1.jpg", "https://storage/third-2.jpg"));
    flushAndClear();

    Slice<PlaceReviewListItem> slice =
        placeReviewQueryRepository.findReviewPageByPlaceId(
            place.getId(), cursor(null, 10), true, PlaceReviewSortType.LATEST);

    assertThat(slice.isHasNext()).isFalse();
    assertThat(slice.getContent())
        .extracting(PlaceReviewListItem::reviewId)
        .containsExactly(thirdReview.getId(), firstReview.getId());
    assertThat(slice.getContent()).allSatisfy(item -> assertThat(item.imageUrls()).isNotEmpty());
  }

  @Test
  @DisplayName("등록순으로 조회하면 오래된 리뷰부터 반환한다")
  void findReviewPageByPlaceId_whenOldest_returnsReviewsByCreatedAtAsc() {
    Place place = placeRepository.save(Place.of("콤파일", "서울 마포구 잔다리로 73", 37.0, 127.0));
    PlaceReview firstReview = saveReview(place, "첫번째", 3, "첫번째 리뷰", List.of());
    PlaceReview secondReview = saveReview(place, "두번째", 4, "두번째 리뷰", List.of());
    PlaceReview thirdReview = saveReview(place, "세번째", 5, "세번째 리뷰", List.of());
    flushAndClear();

    Slice<PlaceReviewListItem> slice =
        placeReviewQueryRepository.findReviewPageByPlaceId(
            place.getId(), cursor(null, 10), false, PlaceReviewSortType.OLDEST);

    assertThat(slice.getContent())
        .extracting(PlaceReviewListItem::reviewId)
        .containsExactly(firstReview.getId(), secondReview.getId(), thirdReview.getId());
  }

  @Test
  @DisplayName("별점 높은 순으로 조회하면 별점이 높은 리뷰부터 반환한다")
  void findReviewPageByPlaceId_whenRatingHigh_returnsReviewsByRatingDesc() {
    Place place = placeRepository.save(Place.of("콤파일", "서울 마포구 잔다리로 73", 37.0, 127.0));
    PlaceReview lowReview = saveReview(place, "낮은별점", 2, "낮은 별점 리뷰", List.of());
    PlaceReview highReview = saveReview(place, "높은별점", 5, "높은 별점 리뷰", List.of());
    PlaceReview middleReview = saveReview(place, "중간별점", 4, "중간 별점 리뷰", List.of());
    flushAndClear();

    Slice<PlaceReviewListItem> slice =
        placeReviewQueryRepository.findReviewPageByPlaceId(
            place.getId(), cursor(null, 10), false, PlaceReviewSortType.RATING_HIGH);

    assertThat(slice.getContent())
        .extracting(PlaceReviewListItem::reviewId)
        .containsExactly(highReview.getId(), middleReview.getId(), lowReview.getId());
  }

  @Test
  @DisplayName("별점 낮은 순으로 조회하면 별점이 낮은 리뷰부터 반환한다")
  void findReviewPageByPlaceId_whenRatingLow_returnsReviewsByRatingAsc() {
    Place place = placeRepository.save(Place.of("콤파일", "서울 마포구 잔다리로 73", 37.0, 127.0));
    PlaceReview lowReview = saveReview(place, "낮은별점", 2, "낮은 별점 리뷰", List.of());
    PlaceReview highReview = saveReview(place, "높은별점", 5, "높은 별점 리뷰", List.of());
    PlaceReview middleReview = saveReview(place, "중간별점", 4, "중간 별점 리뷰", List.of());
    flushAndClear();

    Slice<PlaceReviewListItem> slice =
        placeReviewQueryRepository.findReviewPageByPlaceId(
            place.getId(), cursor(null, 10), false, PlaceReviewSortType.RATING_LOW);

    assertThat(slice.getContent())
        .extracting(PlaceReviewListItem::reviewId)
        .containsExactly(lowReview.getId(), middleReview.getId(), highReview.getId());
  }

  @Test
  @DisplayName("사진 리뷰만 보기를 별점 높은 순과 함께 적용한다")
  void findReviewPageByPlaceId_whenImageOnlyAndRatingHigh_returnsImageReviewsByRatingDesc() {
    Place place = placeRepository.save(Place.of("콤파일", "서울 마포구 잔다리로 73", 37.0, 127.0));
    saveReview(place, "사진없는높은별점", 5, "사진 없는 높은 별점 리뷰", List.of());
    PlaceReview imageMiddleReview =
        saveReview(place, "사진있는중간별점", 4, "사진 있는 중간 별점 리뷰", List.of("middle.jpg"));
    PlaceReview imageLowReview =
        saveReview(place, "사진있는낮은별점", 2, "사진 있는 낮은 별점 리뷰", List.of("low.jpg"));
    flushAndClear();

    Slice<PlaceReviewListItem> slice =
        placeReviewQueryRepository.findReviewPageByPlaceId(
            place.getId(), cursor(null, 10), true, PlaceReviewSortType.RATING_HIGH);

    assertThat(slice.getContent())
        .extracting(PlaceReviewListItem::reviewId)
        .containsExactly(imageMiddleReview.getId(), imageLowReview.getId());
    assertThat(slice.getContent()).allSatisfy(item -> assertThat(item.imageUrls()).isNotEmpty());
  }

  @Test
  @DisplayName("별점 높은 순 커서 페이징이 동작한다")
  void findReviewPageByPlaceId_whenRatingHigh_usesRatingCursor() {
    Place place = placeRepository.save(Place.of("콤파일", "서울 마포구 잔다리로 73", 37.0, 127.0));
    PlaceReview lowReview = saveReview(place, "낮은별점", 2, "낮은 별점 리뷰", List.of());
    PlaceReview middleReview = saveReview(place, "중간별점", 4, "중간 별점 리뷰", List.of());
    PlaceReview highReview = saveReview(place, "높은별점", 5, "높은 별점 리뷰", List.of());
    flushAndClear();

    Slice<PlaceReviewListItem> firstPage =
        placeReviewQueryRepository.findReviewPageByPlaceId(
            place.getId(), cursor(null, 2), false, PlaceReviewSortType.RATING_HIGH);

    assertThat(firstPage.isHasNext()).isTrue();
    assertThat(firstPage.getContent())
        .extracting(PlaceReviewListItem::reviewId)
        .containsExactly(highReview.getId(), middleReview.getId());

    PlaceReviewListItem lastItem = firstPage.getContent().get(1);
    String nextCursor = lastItem.rating() + "|" + lastItem.reviewId();
    Slice<PlaceReviewListItem> secondPage =
        placeReviewQueryRepository.findReviewPageByPlaceId(
            place.getId(), cursor(nextCursor, 2), false, PlaceReviewSortType.RATING_HIGH);

    assertThat(secondPage.isHasNext()).isFalse();
    assertThat(secondPage.getContent())
        .extracting(PlaceReviewListItem::reviewId)
        .containsExactly(lowReview.getId());
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
