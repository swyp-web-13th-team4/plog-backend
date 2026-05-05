package com.plog.plogbackend.domain.map.repository;

import static com.plog.plogbackend.domain.bookmark.entity.QBookMark.bookMark;
import static com.plog.plogbackend.domain.place.entity.QPlace.place;
import static com.plog.plogbackend.domain.post.entity.QPost.post;
import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.bookmark.entity.BookMark;
import com.plog.plogbackend.domain.bookmark.repository.BookMarkRepository;
import com.plog.plogbackend.domain.map.model.Viewport;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostImage;
import com.plog.plogbackend.domain.post.entity.PublicScope;
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
        mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT, cursor(null, 10));

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
        mapQueryRepository.findRecordPinsByMemberId(me.getId(), VIEWPORT, cursor(null, 10));

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
        mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT, cursor(null, 10));

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
            member.getId(), VIEWPORT, cursor(cafe3.getId(), 10));

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
        mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT, cursor(null, 1));
    Slice<Tuple> hasNextFalse =
        mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT, cursor(null, 2));

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
        mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT, cursor(null, 1));

    assertThat(firstPage.getContent()).hasSize(1);
    assertThat(firstPage.isHasNext()).isTrue();
    Long firstId = firstPage.getContent().get(0).get(place.id);
    assertThat(firstId).isEqualTo(cafe3.getId()); // 최신순(DESC)이므로 가장 큰 ID

    // 3. 두 번째 페이지 조회 (첫 번째 페이지의 ID를 커서로 사용)
    Slice<Tuple> secondPage =
        mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT, cursor(firstId, 1));

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
        mapQueryRepository.findRecordPinsByMemberId(member.getId(), VIEWPORT, cursor(null, 10));

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
        mapQueryRepository.findBookmarkPinsByMemberId(member.getId(), VIEWPORT, cursor(null, 10));

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
        mapQueryRepository.findBookmarkPinsByMemberId(me.getId(), VIEWPORT, cursor(null, 10));

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
        mapQueryRepository.findBookmarkPinsByMemberId(me.getId(), VIEWPORT, cursor(null, 10));

    assertThat(result.getContent().get(0).get(bookMark.id.count())).isEqualTo(2L);
  }

  // =====================
  //       helpers
  // =====================

  private void flushAndClear() {
    em.flush();
    em.clear();
  }

  private Cursorable<Long> cursor(Long cursor, int limit) {
    return new Cursorable<>(cursor, limit);
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
            place));
  }
}
