package com.plog.plogbackend.domain.post.entity;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.global.common.entity.BaseTimeStatusEntity;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeStatusEntity {

  // 게시글 도메인 상수
  public static final int MIN_TITLE_LENGTH = 2;
  public static final int MAX_TITLE_LENGTH = 20;
  public static final int MIN_CONTENTS_COUNT = 20;
  public static final int MAX_CONTENTS_COUNT = 300;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  @Column(length = MAX_CONTENTS_COUNT)
  private String contents;

  private LocalDateTime startedAt;
  private LocalDateTime endedAt;

  private LocalDate studyDate;

  // 계산해서 값을 주입
  private Integer studyTime;

  private Integer focus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PublicScope scope;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id")
  private Place place;

  @Enumerated(EnumType.STRING)
  @Column(name = "place_category")
  private PlaceCategoryCode placeCategory;

  private Long likes;

  @OneToMany(
      mappedBy = "post",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<PostImage> images = new ArrayList<>();

  @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
  private List<PostTag> tags = new ArrayList<>();

  @Builder
  private Post(
      String title,
      String contents,
      LocalDateTime startedAt,
      LocalDateTime endedAt,
      LocalDate studyDate,
      Integer studyTime,
      Integer focus,
      PublicScope scope,
      Member member,
      Place place,
      PlaceCategoryCode placeCategory) {

    this.title = title;
    this.contents = contents;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.studyDate = studyDate;
    this.studyTime = studyTime;
    this.focus = focus;
    this.scope = scope;
    this.member = member;
    this.place = place;
    this.placeCategory = placeCategory;
    this.likes = 0L;
  }

  public static Post of(
      String title,
      String contents,
      LocalDateTime startedAt,
      LocalDateTime endedAt,
      LocalDate studyDate,
      Integer focus,
      PublicScope scope,
      Member member,
      Place place,
      PlaceCategoryCode placeCategory) {
    // toIntExact은 캐스팅 실패시 예외를 던져준다
    Integer calculatedStudyTime = Math.toIntExact(Duration.between(startedAt, endedAt).toMinutes());

    return Post.builder()
        .title(title)
        .contents(contents)
        .startedAt(startedAt)
        .endedAt(endedAt)
        // 계산된 값을 넣음
        .studyTime(calculatedStudyTime)
        .studyDate(studyDate)
        .focus(focus)
        .scope(scope)
        .member(member)
        .place(place)
        .placeCategory(placeCategory)
        .build();
  }

  public void update(
      String title,
      String contents,
      LocalDateTime startedAt,
      LocalDateTime endedAt,
      LocalDate studyDate,
      Integer focus,
      PublicScope scope,
      Place place,
      PlaceCategoryCode placeCategory) {
    this.title = title;
    this.contents = contents;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.studyTime = Math.toIntExact(Duration.between(startedAt, endedAt).toMinutes());
    this.studyDate = studyDate;
    this.focus = focus;
    this.scope = scope;
    this.place = place;
    this.placeCategory = placeCategory;
  }

  // 게시글 수정 시 편의 메소드
  public void updateTime(LocalDateTime newStartedAt, LocalDateTime newEndedAt) {
    this.startedAt = newStartedAt;
    this.endedAt = newEndedAt;
    this.studyTime = (int) Duration.between(newStartedAt, newEndedAt).toMinutes();
  }

  public void addImage(PostImage image) {
    this.images.add(image);
  }

  // 편의 메소드
  public void replaceTags(List<PostTag> newTags) {
    this.tags.clear();
    this.tags.addAll(newTags);
  }
}
