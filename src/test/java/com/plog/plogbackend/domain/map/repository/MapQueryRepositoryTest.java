package com.plog.plogbackend.domain.map.repository;

import static com.plog.plogbackend.domain.bookmark.entity.QBookMark.bookMark;
import static com.plog.plogbackend.domain.place.entity.QPlace.place;
import static com.plog.plogbackend.domain.post.entity.QPost.post;
import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.bookmark.entity.BookMark;
import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.map.model.RecordSortType;
import com.plog.plogbackend.domain.map.model.Viewport;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.domain.post.entity.PlaceCategory;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostImage;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.post.repository.PostImageRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import com.querydsl.core.Tuple;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            member.getId(), VIEWPORT, RecordSortType.LATEST, cursor(null, 10));

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
            me.getId(), VIEWPORT, RecordSortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).get(post.id.count())).isEqualTo(1L);
  }

  @Test
  @DisplayName("count, totalStudyTime, avgFocus가 여러 게시글에 대해 올바르게 집계된다")
  void record_집계값_검증() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    savePost(member, cafe, 60, 80); // studyTime=60, focus=80
    savePost(member, cafe, 120, 40); // studyTime=120, focus=40
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, RecordSortType.LATEST, cursor(null, 10));

    Tuple tuple = result.getContent().get(0);
    assertThat(tuple.get(post.id.count())).isEqualTo(2L);
    assertThat(tuple.get(post.studyTime.sum())).isEqualTo(180);
    assertThat(tuple.get(post.focus.avg())).isEqualTo(60.0);
  }

  @Test
  @DisplayName("cursor 이상의 placeId는 반환되지 않는다")
  void record_커서_페이지네이션() {
    Member member = saveMember("user");
    Place cafe1 = savePlace("카페1", 37.5, 127.0);
    Place cafe2 = savePlace("카페2", 37.5, 127.0);
    Place cafe3 = savePlace("카페3", 37.5, 127.0);
    savePost(member, cafe1, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe3, 60, 80);
    flushAndClear();

    // cafe3은 cursor로 사용 → cafe3 자신은 결과에서 제외

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, RecordSortType.LATEST, cursor(cafe3.getId(), 10));

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
            member.getId(), VIEWPORT, RecordSortType.LATEST, cursor(null, 1));
    Slice<Tuple> hasNextFalse =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, RecordSortType.LATEST, cursor(null, 2));

    assertThat(hasNextTrue.isHasNext()).isTrue();
    assertThat(hasNextFalse.isHasNext()).isFalse();
  }

  @Test
  @DisplayName("커서를 이용한 연속 조회 시 데이터가 중복 없이 순서대로 나온다")
  void pagination_Continuous_Success() {
    // 1. 데이터 준비 (ID가 큰 순서대로 3개 생성)
    Member member = saveMember("user");
    Place cafe1 = savePlace("카페1", 37.5, 127.0);
    Place cafe2 = savePlace("카페2", 37.5, 127.0);
    Place cafe3 = savePlace("카페3", 37.5, 127.0);
    savePost(member, cafe1, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe3, 60, 80);
    flushAndClear();

    // 2. 첫 번째 페이지 조회 (Limit 1)
    Slice<Tuple> firstPage =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, RecordSortType.LATEST, cursor(null, 1));

    assertThat(firstPage.getContent()).hasSize(1);
    assertThat(firstPage.isHasNext()).isTrue();
    Long firstId = firstPage.getContent().get(0).get(place.id);
    assertThat(firstId).isEqualTo(cafe3.getId()); // 최신순(DESC)이므로 가장 큰 ID

    // 3. 두 번째 페이지 조회 (첫 번째 페이지의 ID를 커서로 사용)
    Slice<Tuple> secondPage =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, RecordSortType.LATEST, cursor(firstId, 1));

    assertThat(secondPage.getContent()).hasSize(1);
    Long secondId = secondPage.getContent().get(0).get(place.id);
    assertThat(secondId).isEqualTo(cafe2.getId()); // 그 다음으로 큰 ID가 나와야 함
    assertThat(secondId).isLessThan(firstId); // ID가 작아지는 방향(최신순) 확인
  }

  @Test
  @DisplayName("섬네일은 해당 장소에서 가장 최신 게시글의 이미지를 반환한다")
  void record_섬네일_최신_게시글_이미지() {
    Member member = saveMember("user");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post oldPost = savePost(member, cafe, 60, 80);
    Post newPost = savePost(member, cafe, 60, 80);
    postImageRepository.save(PostImage.of("old-image.jpg", oldPost));
    postImageRepository.save(PostImage.of("new-image.jpg", newPost));
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, RecordSortType.LATEST, cursor(null, 10));

    assertThat(result.getContent().get(0).get(8, String.class)).isEqualTo("new-image.jpg");
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
            member.getId(), VIEWPORT, RecordSortType.LATEST, cursor(null, 10));

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
    bookMarkRepository.save(new BookMark(other, otherPost)); // other의 북마크
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findBookmarkPinsByMemberId(
            me.getId(), VIEWPORT, RecordSortType.LATEST, cursor(null, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).get(bookMark.id.count())).isEqualTo(1L);
  }

  @Test
  @DisplayName("같은 장소에 북마크가 여러 개면 count에 모두 집계된다")
  void bookmark_count_집계() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe = savePlace("카페", 37.5, 127.0);
    Post post1 = savePost(me, cafe, 60, 80);
    Post post2 = savePost(other, cafe, 60, 80);
    bookMarkRepository.save(new BookMark(me, post1));
    bookMarkRepository.save(new BookMark(me, post2)); // me가 같은 장소 2개 북마크
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findBookmarkPinsByMemberId(
            me.getId(), VIEWPORT, RecordSortType.LATEST, cursor(null, 10));

    assertThat(result.getContent().get(0).get(bookMark.id.count())).isEqualTo(2L);
  }

  // =====================
  //   저장 개수순 정렬 테스트
  // =====================

  @Test
  @DisplayName("저장 개수순 정렬 시 기록 많은 장소부터 반환된다")
  void record_저장개수순_정렬() {
    Member member = saveMember("user");
    Place cafe1 = savePlace("카페1", 37.5, 127.0); // 1개
    Place cafe2 = savePlace("카페2", 37.5, 127.0); // 3개
    Place cafe3 = savePlace("카페3", 37.5, 127.0); // 2개
    savePost(member, cafe1, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe3, 60, 80);
    savePost(member, cafe3, 60, 80);
    flushAndClear();

    Slice<Tuple> result =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, RecordSortType.RECORD_COUNT, cursor(null, 10));

    List<Long> placeIds = result.getContent().stream().map(t -> t.get(place.id)).toList();
    assertThat(placeIds).containsExactly(cafe2.getId(), cafe3.getId(), cafe1.getId());
  }

  @Test
  @DisplayName("저장 개수순 정렬 시 count:placeId 커서로 다음 페이지를 올바르게 조회한다")
  void record_저장개수순_커서_페이지네이션() {
    Member member = saveMember("user");
    Place cafe1 = savePlace("카페1", 37.5, 127.0); // 1개
    Place cafe2 = savePlace("카페2", 37.5, 127.0); // 3개
    Place cafe3 = savePlace("카페3", 37.5, 127.0); // 2개
    savePost(member, cafe1, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe3, 60, 80);
    savePost(member, cafe3, 60, 80);
    flushAndClear();

    Slice<Tuple> page1 =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, RecordSortType.RECORD_COUNT, cursor(null, 2));

    assertThat(page1.getContent()).hasSize(2);
    assertThat(page1.isHasNext()).isTrue();

    Tuple last = page1.getContent().get(1); // cafe3 (count=2)
    String nextCursor = last.get(post.id.count()) + ":" + last.get(place.id);

    Slice<Tuple> page2 =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, RecordSortType.RECORD_COUNT, cursor(nextCursor, 2));

    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).get(place.id)).isEqualTo(cafe1.getId());
    assertThat(page2.isHasNext()).isFalse();
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
            me.getId(), VIEWPORT, RecordSortType.RECORD_COUNT, cursor(null, 10));

    List<Long> placeIds = result.getContent().stream().map(t -> t.get(place.id)).toList();
    assertThat(placeIds).containsExactly(cafe2.getId(), cafe1.getId());
  }

  @Test
  @DisplayName("북마크 저장 개수순 정렬 시 count:placeId 커서로 다음 페이지를 올바르게 조회한다")
  void bookmark_저장개수순_커서_페이지네이션() {
    Member me = saveMember("me");
    Member other = saveMember("other");
    Place cafe1 = savePlace("카페1", 37.5, 127.0); // 북마크 1개
    Place cafe2 = savePlace("카페2", 37.5, 127.0); // 북마크 3개
    Place cafe3 = savePlace("카페3", 37.5, 127.0); // 북마크 2개
    Post p1 = savePost(me, cafe1, 60, 80);
    Post p2 = savePost(me, cafe2, 60, 80);
    Post p3 = savePost(other, cafe2, 60, 80);
    Post p4 = savePost(other, cafe2, 60, 80);
    Post p5 = savePost(me, cafe3, 60, 80);
    Post p6 = savePost(other, cafe3, 60, 80);
    bookMarkRepository.save(new BookMark(me, p1));
    bookMarkRepository.save(new BookMark(me, p2));
    bookMarkRepository.save(new BookMark(me, p3));
    bookMarkRepository.save(new BookMark(me, p4));
    bookMarkRepository.save(new BookMark(me, p5));
    bookMarkRepository.save(new BookMark(me, p6));
    flushAndClear();

    Slice<Tuple> page1 =
        mapQueryRepository.findBookmarkPinsByMemberId(
            me.getId(), VIEWPORT, RecordSortType.RECORD_COUNT, cursor(null, 2));

    assertThat(page1.getContent()).hasSize(2);
    assertThat(page1.isHasNext()).isTrue();

    Tuple last = page1.getContent().get(1); // cafe3 (count=2)
    String nextCursor = last.get(bookMark.id.count()) + ":" + last.get(place.id);

    Slice<Tuple> page2 =
        mapQueryRepository.findBookmarkPinsByMemberId(
            me.getId(), VIEWPORT, RecordSortType.RECORD_COUNT, cursor(nextCursor, 2));

    assertThat(page2.getContent()).hasSize(1);
    assertThat(page2.getContent().get(0).get(place.id)).isEqualTo(cafe1.getId());
    assertThat(page2.isHasNext()).isFalse();
  }

  @Test
  @DisplayName("저장 개수 동일 시 placeId로 정렬되고, 커서 기반 페이지네이션이 중복 없이 동작한다")
  void record_저장개수순_tie_breaker_커서_검증() {
    Member member = saveMember("user");

    // placeId 순서: cafe1 < cafe2 < cafe3 < cafe4 (자동 증가 가정)
    Place cafe1 = savePlace("카페1", 37.5, 127.0); // count=1
    Place cafe2 = savePlace("카페2", 37.5, 127.0); // count=3
    Place cafe3 = savePlace("카페3", 37.5, 127.0); // count=2
    Place cafe4 = savePlace("카페4", 37.5, 127.0); // count=2 (tie)

    // count 세팅
    savePost(member, cafe1, 60, 80);

    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);
    savePost(member, cafe2, 60, 80);

    savePost(member, cafe3, 60, 80);
    savePost(member, cafe3, 60, 80);

    savePost(member, cafe4, 60, 80);
    savePost(member, cafe4, 60, 80);

    flushAndClear();

    // 1페이지 (limit=2)
    Slice<Tuple> page1 =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, RecordSortType.RECORD_COUNT, cursor(null, 2));

    assertThat(page1.getContent()).hasSize(2);
    assertThat(page1.isHasNext()).isTrue();

    // 기대 순서: count DESC, placeId DESC
    // cafe2(3) -> cafe4(2) (cafe3보다 id 큼)
    List<Long> page1Ids = page1.getContent().stream().map(t -> t.get(place.id)).toList();

    assertThat(page1Ids).containsExactly(cafe2.getId(), cafe4.getId());

    // 커서 생성
    Tuple last = page1.getContent().get(1); // cafe4
    String nextCursor = last.get(post.id.count()) + ":" + last.get(place.id);

    // 2페이지
    Slice<Tuple> page2 =
        mapQueryRepository.findRecordPinsByMemberId(
            member.getId(), VIEWPORT, RecordSortType.RECORD_COUNT, cursor(nextCursor, 2));

    assertThat(page2.getContent()).hasSize(2);

    List<Long> page2Ids = page2.getContent().stream().map(t -> t.get(place.id)).toList();

    // 남은 순서: cafe3(2), cafe1(1)
    assertThat(page2Ids).containsExactly(cafe3.getId(), cafe1.getId());

    // 🔥 핵심: 중복 없음 + 전체 순서 검증
    List<Long> all = new java.util.ArrayList<>();
    all.addAll(page1Ids);
    all.addAll(page2Ids);

    assertThat(all)
        .containsExactly(
            cafe2.getId(), // 3
            cafe4.getId(), // 2 (id 큰 것 먼저)
            cafe3.getId(), // 2
            cafe1.getId() // 1
            );
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
            placeCategory
            ));
  }
}
