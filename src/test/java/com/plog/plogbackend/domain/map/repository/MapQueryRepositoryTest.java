package com.plog.plogbackend.domain.map.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.bookmark.entity.BookMark;
import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.map.repository.dto.MapPin;
import com.plog.plogbackend.domain.map.repository.dto.PlaceRecord;
import com.plog.plogbackend.domain.map.repository.dto.PlaceSearchResult;
import com.plog.plogbackend.domain.map.repository.dto.PlaceSummary;
import com.plog.plogbackend.domain.map.repository.dto.Viewport;
import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostImage;
import com.plog.plogbackend.domain.post.entity.PostTag;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.post.repository.PostImageRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.post.repository.PostTagRepository;
import com.plog.plogbackend.domain.tag.Tag;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.domain.tag.repository.TagRepository;
import com.plog.plogbackend.global.common.enums.SortType;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
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
  @Autowired private TagRepository tagRepository;
  @Autowired private PostTagRepository postTagRepository;
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

    List<MapPin> result = mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getPlaceId()).isEqualTo(inside.getId());
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

    List<MapPin> result = mapQueryRepository.findRecordPinsByMemberId(me.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCount()).isEqualTo(1L);
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

    List<MapPin> result = mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCount()).isEqualTo(3L);
  }

  @Test
  @DisplayName("핀 - 위경도가 올바르게 반환된다")
  void record_위경도_반환() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(member, cafe, 60, 80);
    flushAndClear();

    List<MapPin> result = mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result.get(0).getLatitude()).isEqualTo(37.5);
    assertThat(result.get(0).getLongitude()).isEqualTo(127.0);
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

    List<MapPin> result = mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result.get(0).getThumbnailUrl()).isEqualTo("z-new-image.jpg");
  }

  @Test
  @DisplayName("이미지가 없으면 썸네일은 null이다")
  void record_썸네일_없으면_null() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(member, cafe, 60, 80);
    flushAndClear();

    List<MapPin> result = mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result.get(0).getThumbnailUrl()).isNull();
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

    List<MapPin> result = mapQueryRepository.findBookmarkPinsByMemberId(member.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getPlaceId()).isEqualTo(inside.getId());
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

    List<MapPin> result = mapQueryRepository.findBookmarkPinsByMemberId(me.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCount()).isEqualTo(1L);
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

    List<MapPin> result = mapQueryRepository.findBookmarkPinsByMemberId(me.getId(), VIEWPORT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCount()).isEqualTo(3L);
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

    List<MapPin> result = mapQueryRepository.findBookmarkPinsByMemberId(me.getId(), VIEWPORT);

    assertThat(result.get(0).getThumbnailUrl()).isEqualTo("z-new-image.jpg");
  }

  @Test
  @DisplayName("북마크 핀 - 북마크하지 않은 게시글 이미지는 썸네일에 포함되지 않는다")
  void bookmark_미북마크_썸네일_제외() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post bookmarked = savePost(other, cafe, 60, 80);
    Post notBookmarked = savePost(other, cafe, 60, 80);
    postImageRepository.save(PostImage.of("bookmarked-image.jpg", bookmarked));
    postImageRepository.save(PostImage.of("not-bookmarked-image.jpg", notBookmarked));
    bookMarkRepository.save(new BookMark(me, bookmarked));
    flushAndClear();

    List<MapPin> result = mapQueryRepository.findBookmarkPinsByMemberId(me.getId(), VIEWPORT);

    assertThat(result.get(0).getThumbnailUrl()).isEqualTo("bookmarked-image.jpg");
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

    List<PlaceSearchResult> result =
        mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "스타벅스");

    assertThat(result).hasSize(2);
    List<String> names = result.stream().map(PlaceSearchResult::getPlaceName).toList();
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

    List<PlaceSearchResult> result =
        mapQueryRepository.findRecordedPlacesByKeyword(me.getId(), "스타벅스");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("장소 검색 - 대소문자 무관하게 검색된다")
  void placeSearch_대소문자_무관() {
    Member member = saveMember("user");
    Place place1 = placeRepository.save(Place.of("Starbucks Gangnam", "서울 강남구", 37.5, 127.0));
    savePostWithDate(member, place1, LocalDate.of(2024, 5, 1));
    flushAndClear();

    List<PlaceSearchResult> upper =
        mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "STARBUCKS");
    List<PlaceSearchResult> lower =
        mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "starbucks");

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

    List<PlaceSearchResult> result =
        mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "카페베네");

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

    List<PlaceSearchResult> result =
        mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "스타벅스");

    List<LocalDate> dates = result.stream().map(PlaceSearchResult::getLastStudyDate).toList();
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

    List<PlaceSearchResult> result =
        mapQueryRepository.findRecordedPlacesByKeyword(member.getId(), "스타벅스");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getLastStudyDate()).isEqualTo(LocalDate.of(2024, 6, 15));
  }

  // =====================
  //   장소별 기록/북마크 카테고리 검증
  // =====================

  @Test
  @DisplayName("장소별 기록 조회 - 각 게시글의 카테고리가 실제 저장된 카테고리와 일치한다")
  void placeRecords_카테고리_저장값_일치() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);

    Post p1 = savePostWithCategory(member, cafe, 60, 80, PlaceCategoryCode.CAFE);
    Post p2 = savePostWithCategory(member, cafe, 90, 70, PlaceCategoryCode.STUDY_CAFE);
    flushAndClear();

    Slice<PlaceRecord> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 10));

    assertThat(result.getContent())
        .extracting(PlaceRecord::getPostId)
        .containsExactlyInAnyOrder(p1.getId(), p2.getId());

    PlaceRecord record1 =
        result.getContent().stream()
            .filter(r -> r.getPostId().equals(p1.getId()))
            .findFirst()
            .orElseThrow();
    PlaceRecord record2 =
        result.getContent().stream()
            .filter(r -> r.getPostId().equals(p2.getId()))
            .findFirst()
            .orElseThrow();

    assertThat(record1.getCategoryCode()).isEqualTo(PlaceCategoryCode.CAFE);
    assertThat(record2.getCategoryCode()).isEqualTo(PlaceCategoryCode.STUDY_CAFE);
  }

  @Test
  @DisplayName("장소별 기록 조회 - 카테고리가 없는 게시글은 null을 반환한다")
  void placeRecords_카테고리_null() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePostWithCategory(member, cafe, 60, 80, null);
    flushAndClear();

    Slice<PlaceRecord> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 10));

    assertThat(result.getContent().get(0).getCategoryCode()).isNull();
  }

  @Test
  @DisplayName("장소별 기록 조회 - 태그가 올바르게 반환된다")
  void placeRecords_태그_반환() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p = savePost(member, cafe, 60, 80);
    Tag quietTag = getTag(PlaceTag.QUIET);
    postTagRepository.save(PostTag.of(p, quietTag));
    flushAndClear();

    Slice<PlaceRecord> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 10));

    assertThat(result.getContent().get(0).getTags()).containsExactly(PlaceTag.QUIET);
  }

  @Test
  @DisplayName("장소별 북마크 조회 - 각 게시글의 카테고리가 실제 저장된 카테고리와 일치한다")
  void placeBookmarks_카테고리_저장값_일치() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);

    Post p1 = savePostWithCategory(other, cafe, 60, 80, PlaceCategoryCode.CAFE);
    Post p2 = savePostWithCategory(other, cafe, 90, 70, PlaceCategoryCode.LIBRARY);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    flushAndClear();

    Slice<PlaceRecord> result =
        mapQueryRepository.findBookmarksByPlaceId(
            me.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 10));

    PlaceRecord record1 =
        result.getContent().stream()
            .filter(r -> r.getPostId().equals(p1.getId()))
            .findFirst()
            .orElseThrow();
    PlaceRecord record2 =
        result.getContent().stream()
            .filter(r -> r.getPostId().equals(p2.getId()))
            .findFirst()
            .orElseThrow();

    assertThat(record1.getCategoryCode()).isEqualTo(PlaceCategoryCode.CAFE);
    assertThat(record2.getCategoryCode()).isEqualTo(PlaceCategoryCode.LIBRARY);
  }

  // =====================
  //   정렬 속성 검증
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

    Slice<PlaceRecord> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.STUDY_TIME, null, cursor(null, 10));

    List<Integer> studyTimes = result.getContent().stream().map(PlaceRecord::getStudyTime).toList();
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

    Slice<PlaceRecord> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.FOCUS, null, cursor(null, 10));

    List<Integer> focuses = result.getContent().stream().map(PlaceRecord::getFocus).toList();
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
    Place library = savePlace("도서관", 35.1, 129.0);
    savePost(member, cafe, 60, 80);
    savePost(member, library, 60, 80);
    flushAndClear();

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllRecordPlace(member.getId(), SortType.LATEST, cursor(null, 10));

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

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllRecordPlace(me.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getCount()).isEqualTo(1L);
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

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllRecordPlace(member.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getCount()).isEqualTo(3L);
  }

  @Test
  @DisplayName("시트 기록 - name, address가 올바르게 반환된다")
  void sheetRecord_필드_반환() {
    Member member = saveMember("user");
    Place cafe = placeRepository.save(Place.of("스타벅스 강남", "서울 강남구 테헤란로", 37.5, 127.0));
    savePostWithDate(member, cafe, LocalDate.of(2024, 6, 15));
    flushAndClear();

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllRecordPlace(member.getId(), SortType.LATEST, cursor(null, 10));
    PlaceSummary summary = result.getContent().get(0);

    assertThat(summary.getPlaceName()).isEqualTo("스타벅스 강남");
    assertThat(summary.getAddress()).isEqualTo("서울 강남구 테헤란로");
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

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllRecordPlace(member.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent().get(0).getThumbnailUrl()).isEqualTo("z-new.jpg");
  }

  @Test
  @DisplayName("시트 기록 - 최빈 카테고리가 올바르게 반환된다")
  void sheetRecord_카테고리_최빈값() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePostWithCategory(member, cafe, 60, 80, PlaceCategoryCode.CAFE);
    savePostWithCategory(member, cafe, 60, 80, PlaceCategoryCode.CAFE);
    savePostWithCategory(member, cafe, 60, 80, PlaceCategoryCode.STUDY_CAFE);
    flushAndClear();

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllRecordPlace(member.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent().get(0).getPlaceCategory()).isEqualTo(PlaceCategoryCode.CAFE);
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

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllRecordPlace(member.getId(), SortType.LATEST, cursor(null, 10));

    List<Long> placeIds = result.getContent().stream().map(PlaceSummary::getPlaceId).toList();
    assertThat(placeIds).containsExactly(placeB.getId(), placeC.getId(), placeA.getId());
  }

  @Test
  @DisplayName("시트 기록 - RECORD_COUNT 정렬 시 기록 수 내림차순이다")
  void sheetRecord_RECORD_COUNT_정렬() {
    Member member = saveMember("user");
    Place placeA = savePlace("A", 37.5, 127.0);
    Place placeB = savePlace("B", 37.5, 127.0);
    Place placeC = savePlace("C", 37.5, 127.0);
    savePost(member, placeA, 60, 80);
    savePost(member, placeB, 60, 80);
    savePost(member, placeB, 60, 80);
    savePost(member, placeC, 60, 80);
    savePost(member, placeC, 60, 80);
    savePost(member, placeC, 60, 80);
    flushAndClear();

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllRecordPlace(
            member.getId(), SortType.RECORD_COUNT, cursor(null, 10));

    List<Long> counts = result.getContent().stream().map(PlaceSummary::getCount).toList();
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

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllRecordPlace(member.getId(), SortType.LATEST, cursor(null, 2));

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

    Slice<PlaceSummary> page1 =
        mapQueryRepository.findAllRecordPlace(member.getId(), SortType.LATEST, cursor(null, 2));
    assertThat(page1.isHasNext()).isTrue();

    String rawCursor = decodeNextCursor(page1.getNextCursor());
    Slice<PlaceSummary> page2 =
        mapQueryRepository.findAllRecordPlace(
            member.getId(), SortType.LATEST, cursor(rawCursor, 2));

    assertThat(page2.isHasNext()).isFalse();
    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).getPlaceId()).isEqualTo(placeC.getId());
  }

  @Test
  @DisplayName("시트 기록 - RECORD_COUNT 커서 페이징이 동작한다")
  void sheetRecord_RECORD_COUNT_커서_페이징() {
    Member member = saveMember("user");
    Place placeA = savePlace("A", 37.5, 127.0);
    Place placeB = savePlace("B", 37.5, 127.0);
    Place placeC = savePlace("C", 37.5, 127.0);
    savePost(member, placeA, 60, 80);
    savePost(member, placeA, 60, 80);
    savePost(member, placeA, 60, 80);
    savePost(member, placeB, 60, 80);
    savePost(member, placeB, 60, 80);
    savePost(member, placeC, 60, 80);
    flushAndClear();

    Slice<PlaceSummary> page1 =
        mapQueryRepository.findAllRecordPlace(
            member.getId(), SortType.RECORD_COUNT, cursor(null, 2));
    assertThat(page1.isHasNext()).isTrue();

    String rawCursor = decodeNextCursor(page1.getNextCursor());
    Slice<PlaceSummary> page2 =
        mapQueryRepository.findAllRecordPlace(
            member.getId(), SortType.RECORD_COUNT, cursor(rawCursor, 2));

    assertThat(page2.isHasNext()).isFalse();
    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).getPlaceId()).isEqualTo(placeC.getId());
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
    Place library = savePlace("도서관", 35.1, 129.0);
    Post p1 = savePost(other, cafe, 60, 80);
    Post p2 = savePost(other, library, 60, 80);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    flushAndClear();

    Slice<PlaceSummary> result =
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

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllBookmarkPlaces(me.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getCount()).isEqualTo(2L);
  }

  @Test
  @DisplayName("시트 북마크 - 해당 장소를 북마크한 유저 수가 올바르게 집계된다")
  void sheetBookmark_count_집계() {
    Member me = saveMember("me");
    Member other1 = saveMember("other1");
    Member other2 = saveMember("other2");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(me, cafe, 60, 80);
    Post p2 = savePost(other1, cafe, 60, 80);
    Post p3 = savePost(other2, cafe, 60, 80);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    bookMarkRepository.save(new BookMark(other1, p3));
    bookMarkRepository.save(new BookMark(other2, p3));
    flushAndClear();

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllBookmarkPlaces(me.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getCount()).isEqualTo(3L);
  }

  @Test
  @DisplayName("시트 북마크 - 섬네일은 북마크한 게시글 중 최신 이미지를 반환하며 비북마크 이미지는 제외된다")
  void sheetBookmark_섬네일() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post bookmarked = savePost(other, cafe, 60, 80);
    Post notBookmarked = savePost(other, cafe, 60, 80);
    postImageRepository.save(PostImage.of("bookmarked.jpg", bookmarked));
    postImageRepository.save(PostImage.of("not-bookmarked.jpg", notBookmarked));
    bookMarkRepository.save(new BookMark(me, bookmarked));
    flushAndClear();

    Slice<PlaceSummary> result =
        mapQueryRepository.findAllBookmarkPlaces(me.getId(), SortType.LATEST, cursor(null, 10));

    assertThat(result.getContent().get(0).getThumbnailUrl()).isEqualTo("bookmarked.jpg");
  }

  // =====================
  //   장소별 기록 보완 테스트
  // =====================

  @Test
  @DisplayName("장소별 기록 조회 - 다른 멤버의 기록은 포함되지 않는다")
  void placeRecords_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(me, cafe, 60, 80);
    savePost(other, cafe, 90, 70);
    flushAndClear();

    Slice<PlaceRecord> result =
        mapQueryRepository.findRecordsByPlaceId(
            me.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("장소별 기록 조회 - 썸네일이 올바르게 반환된다")
  void placeRecords_썸네일_반환() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p = savePost(member, cafe, 60, 80);
    postImageRepository.save(PostImage.of("thumb.jpg", p));
    flushAndClear();

    Slice<PlaceRecord> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 10));

    assertThat(result.getContent().get(0).getThumbnailUrl()).isEqualTo("thumb.jpg");
  }

  @Test
  @DisplayName("장소별 기록 조회 - 태그 필터 시 태그가 있는 게시글만 포함된다")
  void placeRecords_태그_필터_동작() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post tagged = savePost(member, cafe, 60, 80);
    Post untagged = savePost(member, cafe, 90, 70);
    Tag quietTag = getTag(PlaceTag.QUIET);
    postTagRepository.save(PostTag.of(tagged, quietTag));
    flushAndClear();

    Slice<PlaceRecord> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(),
            cafe.getId(),
            SortType.LATEST,
            List.of(PlaceTag.QUIET),
            cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getPostId()).isEqualTo(tagged.getId());
  }

  @Test
  @DisplayName("장소별 기록 조회 - limit+1 초과 시 hasNext가 true이다")
  void placeRecords_hasNext() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(member, cafe, 60, 80);
    savePost(member, cafe, 70, 70);
    savePost(member, cafe, 80, 60);
    flushAndClear();

    Slice<PlaceRecord> result =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 2));

    assertThat(result.isHasNext()).isTrue();
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("장소별 기록 조회 - LATEST 커서 페이징이 동작한다")
  void placeRecords_LATEST_커서_페이징() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(member, cafe, 60, 80);
    Post p2 = savePost(member, cafe, 70, 70);
    Post p3 = savePost(member, cafe, 80, 60);
    flushAndClear();

    Slice<PlaceRecord> page1 =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 2));
    assertThat(page1.isHasNext()).isTrue();

    PlaceRecord last = page1.getContent().get(1);
    String nextCursor = String.valueOf(last.getPostId());

    Slice<PlaceRecord> page2 =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.LATEST, null, cursor(nextCursor, 2));

    assertThat(page2.isHasNext()).isFalse();
    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).getPostId()).isEqualTo(p1.getId());
  }

  @Test
  @DisplayName("장소별 기록 조회 - STUDY_TIME 커서 페이징이 동작한다")
  void placeRecords_STUDY_TIME_커서_페이징() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(member, cafe, 30, 80);
    Post p2 = savePost(member, cafe, 60, 70);
    Post p3 = savePost(member, cafe, 90, 60);
    flushAndClear();

    Slice<PlaceRecord> page1 =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.STUDY_TIME, null, cursor(null, 2));
    assertThat(page1.isHasNext()).isTrue();

    PlaceRecord last = page1.getContent().get(1);
    String nextCursor = last.getStudyTime() + "|" + last.getPostId();

    Slice<PlaceRecord> page2 =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.STUDY_TIME, null, cursor(nextCursor, 2));

    assertThat(page2.isHasNext()).isFalse();
    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).getPostId()).isEqualTo(p1.getId());
  }

  @Test
  @DisplayName("장소별 기록 조회 - FOCUS 커서 페이징이 동작한다")
  void placeRecords_FOCUS_커서_페이징() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(member, cafe, 60, 30);
    Post p2 = savePost(member, cafe, 60, 60);
    Post p3 = savePost(member, cafe, 60, 90);
    flushAndClear();

    Slice<PlaceRecord> page1 =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.FOCUS, null, cursor(null, 2));
    assertThat(page1.isHasNext()).isTrue();

    PlaceRecord last = page1.getContent().get(1);
    String nextCursor = last.getFocus() + "|" + last.getPostId();

    Slice<PlaceRecord> page2 =
        mapQueryRepository.findRecordsByPlaceId(
            member.getId(), cafe.getId(), SortType.FOCUS, null, cursor(nextCursor, 2));

    assertThat(page2.isHasNext()).isFalse();
    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).getPostId()).isEqualTo(p1.getId());
  }

  // =====================
  //   장소별 북마크 보완 테스트
  // =====================

  @Test
  @DisplayName("장소별 북마크 조회 - 다른 멤버의 북마크는 포함되지 않는다")
  void placeBookmarks_멤버_격리() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(me, cafe, 60, 80);
    Post p2 = savePost(other, cafe, 90, 70);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(other, p2));
    flushAndClear();

    Slice<PlaceRecord> result =
        mapQueryRepository.findBookmarksByPlaceId(
            me.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("장소별 북마크 조회 - 태그 필터 시 태그가 있는 게시글만 포함된다")
  void placeBookmarks_태그_필터_동작() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post tagged = savePost(other, cafe, 60, 80);
    Post untagged = savePost(other, cafe, 90, 70);
    Tag quietTag = getTag(PlaceTag.QUIET);
    postTagRepository.save(PostTag.of(tagged, quietTag));
    bookMarkRepository.save(new BookMark(me, tagged));
    bookMarkRepository.save(new BookMark(me, untagged));
    flushAndClear();

    Slice<PlaceRecord> result =
        mapQueryRepository.findBookmarksByPlaceId(
            me.getId(), cafe.getId(), SortType.LATEST, List.of(PlaceTag.QUIET), cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getPostId()).isEqualTo(tagged.getId());
  }

  @Test
  @DisplayName("장소별 북마크 조회 - limit+1 초과 시 hasNext가 true이다")
  void placeBookmarks_hasNext() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(other, cafe, 60, 80);
    Post p2 = savePost(other, cafe, 70, 70);
    Post p3 = savePost(other, cafe, 80, 60);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    bookMarkRepository.save(new BookMark(me, p3));
    flushAndClear();

    Slice<PlaceRecord> result =
        mapQueryRepository.findBookmarksByPlaceId(
            me.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 2));

    assertThat(result.isHasNext()).isTrue();
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("장소별 북마크 조회 - LATEST 커서 페이징이 동작한다")
  void placeBookmarks_LATEST_커서_페이징() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post p1 = savePost(other, cafe, 60, 80);
    Post p2 = savePost(other, cafe, 70, 70);
    Post p3 = savePost(other, cafe, 80, 60);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    bookMarkRepository.save(new BookMark(me, p3));
    flushAndClear();

    Slice<PlaceRecord> page1 =
        mapQueryRepository.findBookmarksByPlaceId(
            me.getId(), cafe.getId(), SortType.LATEST, null, cursor(null, 2));
    assertThat(page1.isHasNext()).isTrue();

    PlaceRecord last = page1.getContent().get(1);
    String nextCursor = String.valueOf(last.getPostId());

    Slice<PlaceRecord> page2 =
        mapQueryRepository.findBookmarksByPlaceId(
            me.getId(), cafe.getId(), SortType.LATEST, null, cursor(nextCursor, 2));

    assertThat(page2.isHasNext()).isFalse();
    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).getPostId()).isEqualTo(p1.getId());
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

  private String decodeNextCursor(String encoded) {
    return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
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
            PlaceCategoryCode.CAFE));
  }

  private Tag getTag(PlaceTag placeTag) {
    List<Tag> tags = tagRepository.findByPlaceTag(placeTag);
    if (tags.isEmpty()) {
      throw new IllegalStateException("Tag not initialized for PlaceTag: " + placeTag);
    }
    return tags.get(0);
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
            .placeCategory(PlaceCategoryCode.CAFE)
            .build());
  }

  private Post savePostWithCategory(
      Member member, Place place, int studyMinutes, int focus, PlaceCategoryCode category) {
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
