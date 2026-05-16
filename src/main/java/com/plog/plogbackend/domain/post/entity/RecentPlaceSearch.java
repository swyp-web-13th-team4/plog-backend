package com.plog.plogbackend.domain.post.entity;

import com.plog.plogbackend.domain.member.Member;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "recent_place_search",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_recent_place_member_name_addr",
            columnNames = {"member_id", "place_name", "address"}),
    // 인덱싱 튜닝이 필요한 작업이다
    // 복합 인덱스 카디날리티가 높은 member_id를 첫번쨰로 두번쨰는 최신 검색한것 기준
    // 회원별 최신순 조회 쿼리 가속 — findTop10ByMemberIdOrderBySearchedAtDesc가 이걸 탄다
    indexes =
        @Index(
            name = "idx_recent_place_member_searched",
            columnList = "member_id, searched_at DESC"))
public class RecentPlaceSearch {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false, length = 100)
  private String placeName;

  @Column(nullable = false, length = 200)
  private String address;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  // 최신 검색할때 이 부분을 갱신한다 추후 정렬때 우선 순위에 반영할 수 있다.
  @Column(nullable = false)
  private LocalDateTime searchedAt;

  public static RecentPlaceSearch of(
      Member member, String placeName, String address, Double latitude, Double longitude) {
    RecentPlaceSearch recentPlaceSearch = new RecentPlaceSearch();
    recentPlaceSearch.member = member;
    recentPlaceSearch.placeName = placeName;
    recentPlaceSearch.address = address;
    recentPlaceSearch.latitude = latitude;
    recentPlaceSearch.longitude = longitude;
    recentPlaceSearch.searchedAt = LocalDateTime.now(); // 생성 시점이 곧 검색 시점
    return recentPlaceSearch;
  }

  // 비즈니스에 로직
  public void touch() {
    this.searchedAt = LocalDateTime.now();
  }
}
