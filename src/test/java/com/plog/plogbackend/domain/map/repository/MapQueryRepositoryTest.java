package com.plog.plogbackend.domain.map.repository;

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

  // 서울 강남 근처 뷰포트
  static final Viewport VIEWPORT = Viewport.of(37.4, 126.9, 37.6, 127.1);

  // =====================
  //   내 기록 핀 테스트
  // =====================

  @Test
  @DisplayName("뷰포트 밖 장소의 게시글은 결과에 포함되지 않는다")
  void record_뷰포트_필터링() {
    Member member = saveMember("user1");
    Place inside = savePlace("강남카페", 37.5, 127.0); // 뷰포트 안
    Place outside = savePlace("부산카페", 35.1, 129.0); // 뷰포트 밖
    savePost(member, inside, 60, 80);
    savePost(member, outside, 60, 80);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).get(place.id)).isEqualTo(inside.getId());
  }

  @Test
  @DisplayName("같은 장소라도 다른 멤버의 게시글은 count에 포함되지 않는다")
  void record_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(me, cafe, 60, 80);
    savePost(other, cafe, 120, 90); // 같은 장소, 다른 멤버
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            me.getId(), VIEWPORT, SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).get(5, Long.class)).isEqualTo(1L);
  }

  @Test
  @DisplayName("count, totalStudyTime, avgFocus가 여러 게시글에 대해 올바르게 집계된다")
  void record_집계값_검증() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(member, cafe, 60, 80); // studyTime=60, focus=80
    Post p2 = savePost(member, cafe, 120, 40); // studyTime=120, focus=40

    // 이미지 여러 장 추가 (중복 집계 방지 검증)
    postImageRepository.save(PostImage.of("img1.jpg", p1));
    postImageRepository.save(PostImage.of("img2.jpg", p1));
    postImageRepository.save(PostImage.of("img3.jpg", p2));

    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.LATEST, cursor(null, 10));

    Tuple tuple = result.getContent().get(0);
    assertThat(tuple.get(5, Long.class)).isEqualTo(2L);
    assertThat(tuple.get(6, Integer.class)).isEqualTo(180);
    assertThat(tuple.get(7, Double.class)).isEqualTo(60.0);
  }

  @Test
  @DisplayName("LATEST 정렬 시 placeId 내림차순으로 반환된다")
  void record_최신순_정렬_검증() {
    Member member = saveMember("user");

    // cafe1 < cafe2 < cafe3 순으로 placeId 부여됨
    Place cafe1 = savePlace("카페1", 37.5, 127.0);
    Place cafe2 = savePlace("카페2", 37.5, 127.0);
    Place cafe3 = savePlace("카페3", 37.5, 127.0);
    savePost(member, cafe1, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe3, 60, 80);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.LATEST, cursor(null, 10));

    List<Long> placeIds = result.getContent().stream().map(t -> t.get(place.id)).toList();
    assertThat(placeIds).containsExactly(cafe3.getId(), cafe2.getId(), cafe1.getId());
  }

  @Test
  @DisplayName("cursor 이상의 데이터는 반환되지 않는다 (LATEST)")
  void record_커서_페이지네이션() {
    Member member = saveMember("user");
    Place cafe1 = savePlace("카페1", 37.5, 127.0);
    Place cafe2 = savePlace("카페2", 37.5, 127.0);
    Place cafe3 = savePlace("카페3", 37.5, 127.0);
    savePost(member, cafe1, 60, 80);
    savePost(member, cafe2, 60, 80);
    Post p3 = savePost(member, cafe3, 60, 80);
    flushAndClear();

    // LATEST 정렬 시 커서: placeId
    String cursor = String.valueOf(cafe3.getId());

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.LATEST, cursor(cursor, 10));

    List<Long> placeIds = result.getContent().stream().map(t -> t.get(place.id)).toList();
    assertThat(placeIds).doesNotContain(cafe3.getId());
    assertThat(placeIds).containsExactlyInAnyOrder(cafe1.getId(), cafe2.getId());
  }

  @Test
  @DisplayName("결과가 limit 초과면 hasNext=true, 이하면 hasNext=false")
  void record_hasNext_검증() {
    Member member = saveMember("user");
    Place cafe1 = savePlace("카페1", 37.5, 127.0);
    Place cafe2 = savePlace("카페2", 37.5, 127.0);
    savePost(member, cafe1, 60, 80);
    savePost(member, cafe2, 60, 80);
    flushAndClear();

    Slice<Tuple> hasNextTrue =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.LATEST, cursor(null, 1));
    Slice<Tuple> hasNextFalse =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.LATEST, cursor(null, 2));

    assertThat(hasNextTrue.isHasNext()).isTrue();
    assertThat(hasNextFalse.isHasNext()).isFalse();
  }

  @Test
  @DisplayName("커서를 이용한 연속 조회 시 데이터가 중복 없이 순서대로 나온다")
  void pagination_Continuous_Success() {
    Member member = saveMember("user");
    Place cafe1 = savePlace("카페1", 37.5, 127.0);
    Place cafe2 = savePlace("카페2", 37.5, 127.0);
    Place cafe3 = savePlace("카페3", 37.5, 127.0);
    savePost(member, cafe1, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe3, 60, 80);
    flushAndClear();

    Slice<Tuple> firstPage =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.LATEST, cursor(null, 1));

    assertThat(firstPage.getContent()).hasSize(1);
    assertThat(firstPage.isHasNext()).isTrue();
    Tuple firstTuple = firstPage.getContent().get(0);
    Long firstId = firstTuple.get(place.id);
    assertThat(firstId).isEqualTo(cafe3.getId());

    String firstCursor = firstId.toString();
    Slice<Tuple> secondPage =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.LATEST, cursor(firstCursor, 1));

    assertThat(secondPage.getContent()).hasSize(1);
    Long secondId = secondPage.getContent().get(0).get(place.id);
    assertThat(secondId).isEqualTo(cafe2.getId());
  }

  @Test
  @DisplayName("섬네일은 해당 장소에서 가장 최신 게시글의 이미지를 반환한다")
  void record_섬네일_최신_게시글_이미지() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post oldPost = savePost(member, cafe, 60, 80);
    Post newPost = savePost(member, cafe, 60, 80);
    // a- 가 붙은 이미지가 알파벳 순으론 빠르지만, id가 큰 newPost의 이미지가 나와야 함
    postImageRepository.save(PostImage.of("a-old-image.jpg", oldPost));
    postImageRepository.save(PostImage.of("z-new-image.jpg", newPost));
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent().get(0).get(8, String.class)).isEqualTo("z-new-image.jpg");
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

    Slice<Tuple> result =
        mapQueryRepository.findBookmarkPinsByMemberId(
            member.getId(), VIEWPORT, SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).get(place.id)).isEqualTo(inside.getId());
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

    Slice<Tuple> result =
        mapQueryRepository.findBookmarkPinsByMemberId(
            me.getId(), VIEWPORT, SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).get(5, Long.class)).isEqualTo(1L);
  }

  @Test
  @DisplayName("저장 개수순 정렬 시 기록 많은 장소부터 반환된다")
  void record_저장개수순_정렬() {
    Member member = saveMember("user");
    Place cafe1 = savePlace("카페1", 37.5, 127.0);
    Place cafe2 = savePlace("카페2", 37.5, 127.0);
    Place cafe3 = savePlace("카페3", 37.5, 127.0);
    savePost(member, cafe1, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe3, 60, 80);
    savePost(member, cafe3, 60, 80);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.RECORD_COUNT, cursor(null, 10));

    List<Long> placeIds = result.getContent().stream().map(t -> t.get(place.id)).toList();
    assertThat(placeIds).containsExactly(cafe2.getId(), cafe3.getId(), cafe1.getId());
  }

  @Test
  @DisplayName("저장 개수순 정렬 시 count:placeId 커서로 다음 페이지를 올바르게 조회한다")
  void record_저장개수순_커서_페이지네이션() {
    Member member = saveMember("user");
    Place cafe1 = savePlace("카페1", 37.5, 127.0);
    Place cafe2 = savePlace("카페2", 37.5, 127.0);
    Place cafe3 = savePlace("카페3", 37.5, 127.0);
    savePost(member, cafe1, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe3, 60, 80);
    savePost(member, cafe3, 60, 80);
    flushAndClear();

    Slice<Tuple> page1 =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.RECORD_COUNT, cursor(null, 2));

    assertThat(page1.getContent()).hasSize(2);
    Tuple last = page1.getContent().get(1); // cafe3 (count=2)
    String nextCursor = last.get(5, Long.class) + ":" + last.get(place.id);

    Slice<Tuple> page2 =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.RECORD_COUNT, cursor(nextCursor, 2));

    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).get(place.id)).isEqualTo(cafe1.getId());
  }

  @Test
  @DisplayName("북마크 저장 개수순 정렬 시 북마크 많은 장소부터 반환된다")
  void bookmark_저장개수순_정렬() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe1 = savePlace("카페1", 37.5, 127.0); // 북마크 1개
    Place cafe2 = savePlace("카페2", 37.5, 127.0); // 북마크 2개
    Post p1 = savePost(me, cafe1, 60, 80);
    Post p2 = savePost(me, cafe2, 60, 80);
    Post p3 = savePost(other, cafe2, 60, 80);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    bookMarkRepository.save(new BookMark(me, p3));
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findBookmarkPinsByMemberId(
            me.getId(), VIEWPORT, SortType.RECORD_COUNT, cursor(null, 10));

    List<Long> placeIds = result.getContent().stream().map(t -> t.get(place.id)).toList();
    assertThat(placeIds).containsExactly(cafe2.getId(), cafe1.getId());
  }

  // =====================
  //       helpers
  // =====================

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
    savePostWithCategory(other, cafe, 60, 80, libCat); // 다른 멤버 → 포함 안 됨
    flushAndClear();

    List<Tuple> result =
        mapQueryRepository.findRecordCategoryCountsByPlaceIds(me.getId(), List.of(cafe.getId()));

    // 내 기록(CAFE)만 나와야 함
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
    bookMarkRepository.save(new BookMark(other, otherPost)); // other의 북마크 → 포함 안 됨
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

    // 각 postId → 실제 반환된 카테고리 매핑
    Map<Long, PlaceCategoryCode> returned =
        result.getContent().stream()
            .collect(
                Collectors.toMap(
                    t -> t.get(post).getId(), t -> t.get(post.placeCategory.categoryName)));

    // 저장한 카테고리와 정확히 일치해야 함
    assertThat(returned.get(p1.getId())).isEqualTo(PlaceCategoryCode.CAFE);
    assertThat(returned.get(p2.getId())).isEqualTo(PlaceCategoryCode.STUDY_CAFE);
  }

  @Test
  @DisplayName("장소별 기록 조회 - 카테고리가 없는 게시글은 null을 반환한다")
  void placeRecords_카테고리_null() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p = savePostWithCategory(member, cafe, 60, 80, null); // 카테고리 없음
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

  @Test
  @DisplayName("저장 개수순 정렬 시 반환된 count가 내림차순이다")
  void record_RECORD_COUNT_정렬_속성_검증() {
    Member member = saveMember("user");
    Place cafe1 = savePlace("카페1", 37.5, 127.0);
    Place cafe2 = savePlace("카페2", 37.5, 127.0);
    Place cafe3 = savePlace("카페3", 37.5, 127.0);
    savePost(member, cafe1, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe3, 60, 80);
    savePost(member, cafe3, 60, 80);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, SortType.RECORD_COUNT, cursor(null, 10));

    List<Long> counts = result.getContent().stream().map(t -> t.get(post.id.count())).toList();

    assertThat(counts).isSortedAccordingTo(Comparator.reverseOrder());
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

    // 1. 이미 저장된 'CAFE' 카테고리가 있는지 DB에서 조회
    List<PlaceCategory> categories =
        em.createQuery(
                "select p from PlaceCategory p where p.categoryName = :name", PlaceCategory.class)
            .setParameter("name", PlaceCategoryCode.CAFE)
            .getResultList();

    PlaceCategory placeCategory;
    if (categories.isEmpty()) {
      // 2. 없으면 새로 만들어서 영속화(저장)
      placeCategory = PlaceCategory.builder().categoryName(PlaceCategoryCode.CAFE).build();
      em.persist(placeCategory);
    } else {
      // 3. 있으면 기존에 저장된 객체를 재사용
      placeCategory = categories.get(0);
    }

    // 4. Post 생성 및 저장
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
