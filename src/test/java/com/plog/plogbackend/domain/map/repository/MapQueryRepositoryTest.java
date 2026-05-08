package com.plog.plogbackend.domain.map.repository;

import static com.plog.plogbackend.domain.bookmark.entity.QBookMark.bookMark;
import static com.plog.plogbackend.domain.place.entity.QPlace.place;
import static com.plog.plogbackend.domain.post.entity.QPost.post;
import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.bookmark.entity.BookMark;
import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.map.model.SortType;
import com.plog.plogbackend.domain.map.model.Viewport;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.domain.post.entity.PlaceCategory;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostImage;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.post.repository.PlaceCategoryRepository;
import com.plog.plogbackend.domain.post.repository.PostImageRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import com.querydsl.core.Tuple;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MapQueryRepositoryTest {

  @Autowired private MapQueryRepository mapQueryRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private PlaceRepository placeRepository;
  @Autowired private PostRepository postRepository;
  @Autowired private PostImageRepository postImageRepository;
  @Autowired private BookMarkRepository bookMarkRepository;
  @Autowired private PlaceCategoryRepository placeCategoryRepository;
  @PersistenceContext private EntityManager em;

  static final Viewport VIEWPORT = Viewport.of(37.4, 126.9, 37.6, 127.1);

  // =====================
  //   내 기록 핀 테스트
  // =====================

  @Test
  @DisplayName("뷰포트 밖 장소의 게시글은 결과에 포함되지 않는다")
  void record_뷰포트_필터링() {
    Member member = saveMember("user1");
    Place inside = savePlace("강남카페", 37.5, 127.0);
    Place outside = savePlace("부산카페", 35.1, 129.0);
    savePost(member, inside, 60, 80);
    savePost(member, outside, 60, 80);
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).get(place.id)).isEqualTo(inside.getId());
  }

  @Test
  @DisplayName("같은 장소라도 다른 멤버의 게시글은 count에 포함되지 않는다")
  void record_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(me, cafe, 60, 80);
    savePost(other, cafe, 120, 90);
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findRecordPinsByMemberId(me.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).get(post.id.count())).isEqualTo(1L);
  }

  @Test
  @DisplayName("같은 장소에 여러 기록이 있으면 count가 올바르게 집계된다")
  void record_count_집계() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(member, cafe, 60, 80);
    savePost(member, cafe, 120, 40);
    savePost(member, cafe, 90, 70);
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).get(post.id.count())).isEqualTo(3L);
  }

  @Test
  @DisplayName("핀 - 위경도가 올바르게 반환된다")
  void record_위경도_반환() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(member, cafe, 60, 80);
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result.get(0).get(place.latitude)).isEqualTo(37.5);
    assertThat(result.get(0).get(place.longitude)).isEqualTo(127.0);
  }

  @Test
  @DisplayName("섬네일은 해당 장소에서 가장 최신 이미지를 반환한다")
  void record_섬네일_최신_이미지() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post oldPost = savePost(member, cafe, 60, 80);
    Post newPost = savePost(member, cafe, 60, 80);
    postImageRepository.save(PostImage.of("a-old-image.jpg", oldPost));
    postImageRepository.save(PostImage.of("z-new-image.jpg", newPost));
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result.get(0).get(4, String.class)).isEqualTo("z-new-image.jpg");
  }

  @Test
  @DisplayName("이미지가 없으면 썸네일은 null이다")
  void record_썸네일_없으면_null() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(member, cafe, 60, 80);
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result.get(0).get(4, String.class)).isNull();
  }

  // =====================
  //   북마크 핀 테스트
  // =====================

  @Test
  @DisplayName("뷰포트 밖 장소의 북마크는 결과에 포함되지 않는다")
  void bookmark_뷰포트_필터링() {
    Member member = saveMember("user");
    Place inside = savePlace("강남카페", 37.5, 127.0);
    Place outside = savePlace("부산카페", 35.1, 129.0);
    Post postIn = savePost(member, inside, 60, 80);
    Post postOut = savePost(member, outside, 60, 80);
    bookMarkRepository.save(new BookMark(member, postIn));
    bookMarkRepository.save(new BookMark(member, postOut));
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findBookmarkPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).get(place.id)).isEqualTo(inside.getId());
  }

  @Test
  @DisplayName("다른 멤버의 북마크는 count에 포함되지 않는다")
  void bookmark_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post myPost = savePost(me, cafe, 60, 80);
    Post otherPost = savePost(other, cafe, 60, 80);
    bookMarkRepository.save(new BookMark(me, myPost));
    bookMarkRepository.save(new BookMark(other, otherPost));
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findBookmarkPinsByMemberId(me.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).get(bookMark.id.count())).isEqualTo(1L);
  }

  @Test
  @DisplayName("북마크 핀 - 같은 장소에 여러 북마크가 있으면 count가 올바르게 집계된다")
  void bookmark_count_집계() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(me, cafe, 60, 80);
    Post p2 = savePost(other, cafe, 60, 80);
    Post p3 = savePost(other, cafe, 60, 80);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    bookMarkRepository.save(new BookMark(me, p3));
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findBookmarkPinsByMemberId(me.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).get(bookMark.id.count())).isEqualTo(3L);
  }

  @Test
  @DisplayName("북마크 핀 - 썸네일은 내가 북마크한 게시글 중 가장 최신 이미지를 반환한다")
  void bookmark_섬네일_최신_이미지() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post oldPost = savePost(other, cafe, 60, 80);
    Post newPost = savePost(other, cafe, 60, 80);
    postImageRepository.save(PostImage.of("a-old-image.jpg", oldPost));
    postImageRepository.save(PostImage.of("z-new-image.jpg", newPost));
    bookMarkRepository.save(new BookMark(me, oldPost));
    bookMarkRepository.save(new BookMark(me, newPost));
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findBookmarkPinsByMemberId(me.getId(), VIEWPORT);

    assertThat(result.get(0).get(4, String.class)).isEqualTo("z-new-image.jpg");
  }

  @Test
  @DisplayName("북마크 핀 - 북마크하지 않은 게시글 이미지는 썸네일에 포함되지 않는다")
  void bookmark_미북마크_썸네일_제외() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post bookmarked = savePost(other, cafe, 60, 80);
    Post notBookmarked = savePost(other, cafe, 60, 80); // id가 더 크지만 북마크 안 함
    postImageRepository.save(PostImage.of("bookmarked-image.jpg", bookmarked));
    postImageRepository.save(PostImage.of("not-bookmarked-image.jpg", notBookmarked));
    bookMarkRepository.save(new BookMark(me, bookmarked));
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findBookmarkPinsByMemberId(me.getId(), VIEWPORT);

    assertThat(result.get(0).get(4, String.class)).isEqualTo("bookmarked-image.jpg");
  }

  // =====================
  //   장소 검색 테스트
  // =====================

  @Test
  @DisplayName("장소 검색 - 키워드에 해당하는 내 기록 있는 장소가 반환된다")
  void placeSearch_키워드_기록있는_장소_반환() {
    Member member = saveMember("user");
    Place starbucksGangnam = placeRepository.save(Place.of("스타벅스 강남점", "서울 강남구", 37.5, 127.0));
    Place starbucksYeoksam = placeRepository.save(Place.of("스타벅스 역삼점", "서울 강남구 역삼", 37.49, 127.01));
    Place cafe = savePlace("카페베네", 37.5, 127.0);
    savePostWithDate(member, starbucksGangnam, LocalDate.of(2024, 5, 1));
    savePostWithDate(member, starbucksYeoksam, LocalDate.of(2024, 4, 1));
    savePostWithDate(member, cafe, LocalDate.of(2024, 3, 1));
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "스타벅스");

    assertThat(result).hasSize(2);
    List<String> names = result.stream().map(t -> t.get(place.name)).toList();
    assertThat(names).containsExactlyInAnyOrder("스타벅스 강남점", "스타벅스 역삼점");
  }

  @Test
  @DisplayName("장소 검색 - 다른 멤버의 기록은 포함되지 않는다")
  void placeSearch_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place starbucks = placeRepository.save(Place.of("스타벅스 강남점", "서울 강남구", 37.5, 127.0));
    savePostWithDate(other, starbucks, LocalDate.of(2024, 5, 1));
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findRecordedPlacesByKeyword(me.getId(), "스타벅스");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("장소 검색 - 대소문자 무관하게 검색된다")
  void placeSearch_대소문자_무관() {
    Member member = saveMember("user");
    Place place1 = placeRepository.save(Place.of("Starbucks Gangnam", "서울 강남구", 37.5, 127.0));
    savePostWithDate(member, place1, LocalDate.of(2024, 5, 1));
    flushAndClear();

    List<Tuple> upper = mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "STARBUCKS");
    List<Tuple> lower = mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "starbucks");

    assertThat(upper).hasSize(1);
    assertThat(lower).hasSize(1);
  }

  @Test
  @DisplayName("장소 검색 - 키워드 불일치 시 빈 목록을 반환한다")
  void placeSearch_불일치_빈목록() {
    Member member = saveMember("user");
    Place starbucks = placeRepository.save(Place.of("스타벅스 강남점", "서울 강남구", 37.5, 127.0));
    savePostWithDate(member, starbucks, LocalDate.of(2024, 5, 1));
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "카페베네");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("장소 검색 - 마지막 공부 날짜 내림차순으로 정렬된다")
  void placeSearch_최신_공부날짜_내림차순() {
    Member member = saveMember("user");
    Place starbucksA = placeRepository.save(Place.of("스타벅스 A점", "서울 강남구 A", 37.5, 127.0));
    Place starbucksB = placeRepository.save(Place.of("스타벅스 B점", "서울 강남구 B", 37.49, 127.01));
    Place starbucksC = placeRepository.save(Place.of("스타벅스 C점", "서울 강남구 C", 37.48, 127.02));
    savePostWithDate(member, starbucksA, LocalDate.of(2024, 1, 1));
    savePostWithDate(member, starbucksB, LocalDate.of(2024, 3, 1));
    savePostWithDate(member, starbucksC, LocalDate.of(2024, 2, 1));
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "스타벅스");

    List<LocalDate> dates = result.stream().map(t -> t.get(post.studyDate.max())).toList();
    assertThat(dates).isSortedAccordingTo(Comparator.reverseOrder());
  }

  @Test
  @DisplayName("장소 검색 - 같은 장소에 여러 기록 있을 때 마지막 공부 날짜가 정확히 반환된다")
  void placeSearch_마지막_공부날짜_정확성() {
    Member member = saveMember("user");
    Place starbucks = placeRepository.save(Place.of("스타벅스 강남점", "서울 강남구", 37.5, 127.0));
    savePostWithDate(member, starbucks, LocalDate.of(2024, 1, 1));
    savePostWithDate(member, starbucks, LocalDate.of(2024, 6, 15));
    savePostWithDate(member, starbucks, LocalDate.of(2024, 3, 10));
    flushAndClear();

    List<Tuple> result = mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "스타벅스");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).get(post.studyDate.max())).isEqualTo(LocalDate.of(2024, 6, 15));
  }

  // =====================
  //   카테고리 최빈값 조회 테스트
  // =====================

  @Test
  @DisplayName("기록 핀 카테고리 카운트 - 카테고리별 실제 count가 저장된 개수와 일치한다")
  void recordCategoryCounts_카운트_정확성() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    PlaceCategory cafeCat = saveCategory(PlaceCategoryCode.CAFE);
    PlaceCategory studyCat = saveCategory(PlaceCategoryCode.STUDY_CAFE);

    savePostWithCategory(member, cafe, 60, 80, cafeCat);
    savePostWithCategory(member, cafe, 60, 80, cafeCat);
    savePostWithCategory(member, cafe, 60, 80, studyCat);
    flushAndClear();

    List<Tuple> result =
        mapQueryRepository.findRecordCategoryCountsByPlaceIds(
            member.getId(), List.of(cafe.getId()));

    Map<PlaceCategoryCode, Long> countMap =
        result.stream()
            .collect(
                Collectors.toMap(
                    t -> t.get(1, PlaceCategoryCode.class), t -> t.get(2, Long.class)));

    assertThat(countMap.get(PlaceCategoryCode.CAFE)).isEqualTo(2L);
    assertThat(countMap.get(PlaceCategoryCode.STUDY_CAFE)).isEqualTo(1L);
  }

  @Test
  @DisplayName("기록 핀 카테고리 카운트 - 다른 멤버의 기록은 집계에 포함되지 않는다")
  void recordCategoryCounts_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    PlaceCategory cafeCat = saveCategory(PlaceCategoryCode.CAFE);
    PlaceCategory libCat = saveCategory(PlaceCategoryCode.LIBRARY);

    savePostWithCategory(me, cafe, 60, 80, cafeCat);
    savePostWithCategory(other, cafe, 60, 80, libCat);
    flushAndClear();

    List<Tuple> result =
        mapQueryRepository.findRecordCategoryCountsByPlaceIds(me.getId(), List.of(cafe.getId()));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).get(1, PlaceCategoryCode.class)).isEqualTo(PlaceCategoryCode.CAFE);
  }

  @Test
  @DisplayName("기록 핀 카테고리 카운트 - 여러 장소에 대해 각각 카운트가 독립적으로 집계된다")
  void recordCategoryCounts_장소별_독립_집계() {
    Member member = saveMember("user");
    Place cafe1 = savePlace("카페1", 37.5, 127.0);
    Place cafe2 = savePlace("카페2", 37.5, 127.0);
    PlaceCategory cafeCat = saveCategory(PlaceCategoryCode.CAFE);
    PlaceCategory libCat = saveCategory(PlaceCategoryCode.LIBRARY);

    savePostWithCategory(member, cafe1, 60, 80, cafeCat);
    savePostWithCategory(member, cafe2, 60, 80, libCat);
    savePostWithCategory(member, cafe2, 60, 80, libCat);
    flushAndClear();

    List<Tuple> result =
        mapQueryRepository.findRecordCategoryCountsByPlaceIds(
            member.getId(), List.of(cafe1.getId(), cafe2.getId()));

    Map<Long, Map<PlaceCategoryCode, Long>> byPlace =
        result.stream()
            .collect(
                Collectors.groupingBy(
                    t -> t.get(0, Long.class),
                    Collectors.toMap(
                        t -> t.get(1, PlaceCategoryCode.class), t -> t.get(2, Long.class))));

    assertThat(byPlace.get(cafe1.getId()).get(PlaceCategoryCode.CAFE)).isEqualTo(1L);
    assertThat(byPlace.get(cafe2.getId()).get(PlaceCategoryCode.LIBRARY)).isEqualTo(2L);
  }

  @Test
  @DisplayName("북마크 핀 카테고리 카운트 - 북마크한 게시글의 카테고리별 count가 일치한다")
  void bookmarkCategoryCounts_카운트_정확성() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    PlaceCategory cafeCat = saveCategory(PlaceCategoryCode.CAFE);
    PlaceCategory studyCat = saveCategory(PlaceCategoryCode.STUDY_CAFE);

    Post p1 = savePostWithCategory(other, cafe, 60, 80, cafeCat);
    Post p2 = savePostWithCategory(other, cafe, 60, 80, cafeCat);
    Post p3 = savePostWithCategory(other, cafe, 60, 80, studyCat);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    bookMarkRepository.save(new BookMark(me, p3));
    flushAndClear();

    List<Tuple> result =
        mapQueryRepository.findBookmarkCategoryCountsByPlaceIds(me.getId(), List.of(cafe.getId()));

    Map<PlaceCategoryCode, Long> countMap =
        result.stream()
            .collect(
                Collectors.toMap(
                    t -> t.get(1, PlaceCategoryCode.class), t -> t.get(2, Long.class)));

    assertThat(countMap.get(PlaceCategoryCode.CAFE)).isEqualTo(2L);
    assertThat(countMap.get(PlaceCategoryCode.STUDY_CAFE)).isEqualTo(1L);
  }

  @Test
  @DisplayName("북마크 핀 카테고리 카운트 - 내 북마크가 아닌 기록은 집계에 포함되지 않는다")
  void bookmarkCategoryCounts_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    PlaceCategory cafeCat = saveCategory(PlaceCategoryCode.CAFE);
    PlaceCategory libCat = saveCategory(PlaceCategoryCode.LIBRARY);

    Post myPost = savePostWithCategory(other, cafe, 60, 80, cafeCat);
    Post otherPost = savePostWithCategory(other, cafe, 60, 80, libCat);
    bookMarkRepository.save(new BookMark(me, myPost));
    bookMarkRepository.save(new BookMark(other, otherPost));
    flushAndClear();

    List<Tuple> result =
        mapQueryRepository.findBookmarkCategoryCountsByPlaceIds(me.getId(), List.of(cafe.getId()));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).get(1, PlaceCategoryCode.class)).isEqualTo(PlaceCategoryCode.CAFE);
  }

  // =====================
  //   장소별 기록/북마크 카테고리 검증
  // =====================

  @Test
  @DisplayName("장소별 기록 조회 - 각 게시글의 카테고리가 실제 저장된 카테고리와 일치한다")
  void placeRecords_카테고리_저장값_일치() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    PlaceCategory cafeCat = saveCategory(PlaceCategoryCode.CAFE);
    PlaceCategory studyCat = saveCategory(PlaceCategoryCode.STUDY_CAFE);

    Post p1 = savePostWithCategory(member, cafe, 60, 80, cafeCat);
    Post p2 = savePostWithCategory(member, cafe, 90, 70, studyCat);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 10));

    Map<Long, PlaceCategoryCode> returned =
        result.getContent().stream()
            .collect(
                Collectors.toMap(
                    t -> t.get(post).getId(), t -> t.get(post.placeCategory.categoryName)));

    assertThat(returned.get(p1.getId())).isEqualTo(PlaceCategoryCode.CAFE);
    assertThat(returned.get(p2.getId())).isEqualTo(PlaceCategoryCode.STUDY_CAFE);
  }

  @Test
  @DisplayName("장소별 기록 조회 - 카테고리가 없는 게시글은 null을 반환한다")
  void placeRecords_카테고리_null() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePostWithCategory(member, cafe, 60, 80, null);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 10));

    assertThat(result.getContent().get(0).get(post.placeCategory.categoryName)).isNull();
  }

  @Test
  @DisplayName("장소별 북마크 조회 - 각 게시글의 카테고리가 실제 저장된 카테고리와 일치한다")
  void placeBookmarks_카테고리_저장값_일치() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    PlaceCategory cafeCat = saveCategory(PlaceCategoryCode.CAFE);
    PlaceCategory libCat = saveCategory(PlaceCategoryCode.LIBRARY);

    Post p1 = savePostWithCategory(other, cafe, 60, 80, cafeCat);
    Post p2 = savePostWithCategory(other, cafe, 90, 70, libCat);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findBookmarksByPlaceId(
            me.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 10));

    Map<Long, PlaceCategoryCode> returned =
        result.getContent().stream()
            .collect(
                Collectors.toMap(
                    t -> t.get(post).getId(), t -> t.get(post.placeCategory.categoryName)));

    assertThat(returned.get(p1.getId())).isEqualTo(PlaceCategoryCode.CAFE);
    assertThat(returned.get(p2.getId())).isEqualTo(PlaceCategoryCode.LIBRARY);
  }

  // =====================
  //   정렬 속성 검증 (값 기반)
  // =====================

  @Test
  @DisplayName("장소별 기록 - STUDY_TIME 정렬 시 반환된 studyTime이 내림차순이다")
  void placeRecords_STUDY_TIME_정렬_속성_검증() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(member, cafe, 30, 80);
    savePost(member, cafe, 90, 70);
    savePost(member, cafe, 60, 90);
    savePost(member, cafe, 45, 85);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.STUDY_TIME, null, cursor(null, 10));

    List<Integer> studyTimes =
        result.getContent().stream().map(t -> t.get(post).getStudyTime()).toList();

    assertThat(studyTimes).isSortedAccordingTo(Comparator.reverseOrder());
  }

  @Test
  @DisplayName("장소별 기록 - FOCUS 정렬 시 반환된 focus가 내림차순이다")
  void placeRecords_FOCUS_정렬_속성_검증() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(member, cafe, 60, 50);
    savePost(member, cafe, 60, 90);
    savePost(member, cafe, 60, 70);
    savePost(member, cafe, 60, 30);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.FOCUS, null, cursor(null, 10));

    List<Integer> focuses = result.getContent().stream().map(t -> t.get(post).getFocus()).toList();

    assertThat(focuses).isSortedAccordingTo(Comparator.reverseOrder());
  }

  // =====================
  //   기록/북마크 개수 조회
  // =====================

  @Test
  @DisplayName("기록 개수 - 내 게시글 수만 반환된다")
  void recordCount_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(me, cafe, 60, 80);
    savePost(me, cafe, 60, 80);
    savePost(other, cafe, 60, 80);
    flushAndClear();

    assertThat(postRepository.countByMemberId(me.getId())).isEqualTo(2L);
  }

  @Test
  @DisplayName("북마크 개수 - 내 북마크 수만 반환된다")
  void bookmarkCount_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(me, cafe, 60, 80);
    Post p2 = savePost(me, cafe, 60, 80);
    Post p3 = savePost(other, cafe, 60, 80);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    bookMarkRepository.save(new BookMark(other, p3));
    flushAndClear();

    assertThat(bookMarkRepository.countByMemberId(me.getId())).isEqualTo(2L);
  }

  // =====================
  //   하단 시트 기록 조회 테스트
  // =====================

  @Test
  @DisplayName("시트 기록 - 뷰포트 없이 내 기록 있는 전체 장소를 반환한다")
  void sheetRecord_전체_장소_반환() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Place library = savePlace("도서관", 35.1, 129.0); // 뷰포트 밖이어도 포함
    savePost(member, cafe, 60, 80);
    savePost(member, library, 60, 80);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllRecordPlaces(member.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("시트 기록 - 다른 멤버의 기록은 포함되지 않는다")
  void sheetRecord_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(me, cafe, 60, 80);
    savePost(other, cafe, 60, 80);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllRecordPlaces(me.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).get(post.id.count())).isEqualTo(1L);
  }

  @Test
  @DisplayName("시트 기록 - 같은 장소에 여러 기록이 있으면 count가 집계된다")
  void sheetRecord_count_집계() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(member, cafe, 60, 80);
    savePost(member, cafe, 90, 70);
    savePost(member, cafe, 120, 60);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllRecordPlaces(member.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).get(post.id.count())).isEqualTo(3L);
  }

  @Test
  @DisplayName("시트 기록 - name, address, lastStudyDate가 올바르게 반환된다")
  void sheetRecord_필드_반환() {
    Member member = saveMember("user");
    Place cafe = placeRepository.save(Place.of("스타벅스 강남", "서울 강남구 테헤란로", 37.5, 127.0));
    savePostWithDate(member, cafe, LocalDate.of(2024, 6, 15));
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllRecordPlaces(member.getId(), SortType.LATEST, cursor(null, 10));
    Tuple t = result.getContent().get(0);

    assertThat(t.get(place.name)).isEqualTo("스타벅스 강남");
    assertThat(t.get(place.address)).isEqualTo("서울 강남구 테헤란로");
    assertThat(t.get(post.studyDate.max())).isEqualTo(LocalDate.of(2024, 6, 15));
  }

  @Test
  @DisplayName("시트 기록 - 섬네일은 해당 장소의 가장 최신 이미지를 반환한다")
  void sheetRecord_섬네일() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post oldPost = savePost(member, cafe, 60, 80);
    Post newPost = savePost(member, cafe, 60, 80);
    postImageRepository.save(PostImage.of("a-old.jpg", oldPost));
    postImageRepository.save(PostImage.of("z-new.jpg", newPost));
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllRecordPlaces(member.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent().get(0).get(6, String.class)).isEqualTo("z-new.jpg");
  }

  @Test
  @DisplayName("시트 기록 - LATEST 정렬 시 최신 공부 날짜 내림차순이다")
  void sheetRecord_LATEST_정렬() {
    Member member = saveMember("user");
    Place placeA = savePlace("A", 37.5, 127.0);
    Place placeB = savePlace("B", 37.5, 127.0);
    Place placeC = savePlace("C", 37.5, 127.0);
    savePostWithDate(member, placeA, LocalDate.of(2024, 1, 1));
    savePostWithDate(member, placeB, LocalDate.of(2024, 3, 1));
    savePostWithDate(member, placeC, LocalDate.of(2024, 2, 1));
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllRecordPlaces(member.getId(), SortType.LATEST, cursor(null, 10));

    List<LocalDate> dates =
        result.getContent().stream().map(t -> t.get(post.studyDate.max())).toList();
    assertThat(dates).isSortedAccordingTo(Comparator.reverseOrder());
  }

  @Test
  @DisplayName("시트 기록 - RECORD_COUNT 정렬 시 기록 수 내림차순이다")
  void sheetRecord_RECORD_COUNT_정렬() {
    Member member = saveMember("user");
    Place placeA = savePlace("A", 37.5, 127.0); // count=1
    Place placeB = savePlace("B", 37.5, 127.0); // count=2
    Place placeC = savePlace("C", 37.5, 127.0); // count=3
    savePost(member, placeA, 60, 80);
    savePost(member, placeB, 60, 80);
    savePost(member, placeB, 60, 80);
    savePost(member, placeC, 60, 80);
    savePost(member, placeC, 60, 80);
    savePost(member, placeC, 60, 80);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllRecordPlaces(
            member.getId(), SortType.RECORD_COUNT, cursor(null, 10));

    List<Long> counts =
        result.getContent().stream().map(t -> t.get(post.id.count())).toList();
    assertThat(counts).isSortedAccordingTo(Comparator.reverseOrder());
  }

  @Test
  @DisplayName("시트 기록 - limit 초과 시 hasNext가 true이다")
  void sheetRecord_hasNext() {
    Member member = saveMember("user");
    savePost(member, savePlace("A", 37.5, 127.0), 60, 80);
    savePost(member, savePlace("B", 37.5, 127.0), 60, 80);
    savePost(member, savePlace("C", 37.5, 127.0), 60, 80);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllRecordPlaces(member.getId(), SortType.LATEST, cursor(null, 2));

    assertThat(result.isHasNext()).isTrue();
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("시트 기록 - LATEST 커서 페이징이 동작한다")
  void sheetRecord_LATEST_커서_페이징() {
    Member member = saveMember("user");
    Place placeA = savePlace("A", 37.5, 127.0);
    Place placeB = savePlace("B", 37.5, 127.0);
    Place placeC = savePlace("C", 37.5, 127.0);
    savePostWithDate(member, placeA, LocalDate.of(2024, 3, 1));
    savePostWithDate(member, placeB, LocalDate.of(2024, 2, 1));
    savePostWithDate(member, placeC, LocalDate.of(2024, 1, 1));
    flushAndClear();

    Slice<Tuple> page1 =
        mapQueryRepository.findAllRecordPlaces(member.getId(), SortType.LATEST, cursor(null, 2));
    assertThat(page1.isHasNext()).isTrue();

    Tuple last = page1.getContent().get(1);
    String nextCursor = last.get(post.studyDate.max()) + ":" + last.get(place.id);

    Slice<Tuple> page2 =
        mapQueryRepository.findAllRecordPlaces(
            member.getId(), SortType.LATEST, cursor(nextCursor, 2));

    assertThat(page2.isHasNext()).isFalse();
    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).get(place.id)).isEqualTo(placeC.getId());
  }

  @Test
  @DisplayName("시트 기록 - RECORD_COUNT 커서 페이징이 동작한다")
  void sheetRecord_RECORD_COUNT_커서_페이징() {
    Member member = saveMember("user");
    Place placeA = savePlace("A", 37.5, 127.0); // count=3
    Place placeB = savePlace("B", 37.5, 127.0); // count=2
    Place placeC = savePlace("C", 37.5, 127.0); // count=1
    savePost(member, placeA, 60, 80);
    savePost(member, placeA, 60, 80);
    savePost(member, placeA, 60, 80);
    savePost(member, placeB, 60, 80);
    savePost(member, placeB, 60, 80);
    savePost(member, placeC, 60, 80);
    flushAndClear();

    Slice<Tuple> page1 =
        mapQueryRepository.findAllRecordPlaces(
            member.getId(), SortType.RECORD_COUNT, cursor(null, 2));
    assertThat(page1.isHasNext()).isTrue();

    Tuple last = page1.getContent().get(1);
    String nextCursor = last.get(post.id.count()) + ":" + last.get(place.id);

    Slice<Tuple> page2 =
        mapQueryRepository.findAllRecordPlaces(
            member.getId(), SortType.RECORD_COUNT, cursor(nextCursor, 2));

    assertThat(page2.isHasNext()).isFalse();
    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).get(place.id)).isEqualTo(placeC.getId());
  }

  // =====================
  //   하단 시트 북마크 조회 테스트
  // =====================

  @Test
  @DisplayName("시트 북마크 - 뷰포트 없이 내 북마크 있는 전체 장소를 반환한다")
  void sheetBookmark_전체_장소_반환() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Place library = savePlace("도서관", 35.1, 129.0); // 뷰포트 밖이어도 포함
    Post p1 = savePost(other, cafe, 60, 80);
    Post p2 = savePost(other, library, 60, 80);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllBookmarkPlaces(me.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("시트 북마크 - 다른 멤버의 북마크는 포함되지 않는다")
  void sheetBookmark_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(other, cafe, 60, 80);
    Post p2 = savePost(other, cafe, 60, 80);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(other, p2));
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllBookmarkPlaces(me.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).get(bookMark.id.count())).isEqualTo(1L);
  }

  @Test
  @DisplayName("시트 북마크 - 한 장소에 대한 북마크 count가 올바르게 집계된다")
  void sheetBookmark_count_집계() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(other, cafe, 60, 80);
    Post p2 = savePost(other, cafe, 60, 80);
    Post p3 = savePost(other, cafe, 60, 80);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    bookMarkRepository.save(new BookMark(me, p3));
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllBookmarkPlaces(me.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).get(bookMark.id.count())).isEqualTo(3L);
  }

  @Test
  @DisplayName("시트 북마크 - 섬네일은 북마크한 게시글 중 최신 이미지를 반환하며 비북마크 이미지는 제외된다")
  void sheetBookmark_섬네일() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post bookmarked = savePost(other, cafe, 60, 80);
    Post notBookmarked = savePost(other, cafe, 60, 80); // id 더 큰 미북마크
    postImageRepository.save(PostImage.of("bookmarked.jpg", bookmarked));
    postImageRepository.save(PostImage.of("not-bookmarked.jpg", notBookmarked));
    bookMarkRepository.save(new BookMark(me, bookmarked));
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findAllBookmarkPlaces(me.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent().get(0).get(6, String.class)).isEqualTo("bookmarked.jpg");
  }

  // =====================
  //       helpers
  // =====================

  private void flushAndClear() {
    em.flush();
    em.clear();
  }

  private Cursorable<String> cursor(Object cursor, int limit) {
    return new Cursorable<>(cursor == null ? null : String.valueOf(cursor), limit);
  }

  private Member saveMember(String nickname) {
    return memberRepository.save(
        Member.createNewMember(nickname, "provider-" + nickname, null, null));
  }

  private Place savePlace(String name, double lat, double lng) {
    return placeRepository.save(Place.of(name, "주소-" + name, lat, lng));
  }

  private Post savePost(Member member, Place place, int studyMinutes, int focus) {
    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 9, 0);

    List<PlaceCategory> categories =
        em.createQuery(
                "select p from PlaceCategory p where p.categoryName = :name", PlaceCategory.class)
            .setParameter("name", PlaceCategoryCode.CAFE)
            .getResultList();

    PlaceCategory placeCategory;
    if (categories.isEmpty()) {
      placeCategory = PlaceCategory.builder().categoryName(PlaceCategoryCode.CAFE).build();
      em.persist(placeCategory);
    } else {
      placeCategory = categories.get(0);
    }

    return postRepository.save(
        Post.of(
            "title",
            "contents",
            start,
            start.plusMinutes(studyMinutes),
            LocalDate.of(2024, 1, 1),
            focus,
            PublicScope.PRIVATE,
            member,
            place,
            placeCategory));
  }

  private PlaceCategory saveCategory(PlaceCategoryCode code) {
    return placeCategoryRepository
        .findByCategoryName(code)
        .orElseThrow(() -> new IllegalStateException("PlaceCategory not initialized: " + code));
  }

  private Post savePostWithDate(Member member, Place place, LocalDate studyDate) {
    LocalDateTime start = LocalDateTime.of(studyDate, java.time.LocalTime.of(9, 0));
    return postRepository.save(
        Post.builder()
            .title("title")
            .contents("contents")
            .startedAt(start)
            .endedAt(start.plusMinutes(60))
            .studyDate(studyDate)
            .studyTime(60)
            .focus(80)
            .scope(PublicScope.PRIVATE)
            .member(member)
            .place(place)
            .build());
  }

  private Post savePostWithCategory(
      Member member, Place place, int studyMinutes, int focus, PlaceCategory category) {
    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 9, 0);
    return postRepository.save(
        Post.builder()
            .title("title")
            .contents("contents")
            .startedAt(start)
            .endedAt(start.plusMinutes(studyMinutes))
            .studyDate(LocalDate.of(2024, 1, 1))
            .studyTime(studyMinutes)
            .focus(focus)
            .scope(PublicScope.PRIVATE)
            .member(member)
            .place(place)
            .placeCategory(category)
            .build());
  }
}
