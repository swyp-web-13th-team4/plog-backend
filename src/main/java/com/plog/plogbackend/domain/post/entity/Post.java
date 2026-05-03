package com.plog.plogbackend.domain.post.entity;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.global.common.entity.BaseTimeStatusEntity;
import jakarta.persistence.*;
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

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  private String contents;

  private LocalDateTime startedAt;
  private LocalDateTime endedAt;

  private LocalDate studyDate;

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

  private Long likes;

  @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
  private List<PostImage> images = new ArrayList<>();

  @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
  private List<PostTag> tags = new ArrayList<>();

  @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
  private List<PostCategory> categories = new ArrayList<>();

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
      Place place) {
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
    this.likes = 0L;
  }

  public static Post of(
      String title,
      String contents,
      LocalDateTime startedAt,
      LocalDateTime endedAt,
      LocalDate studyDate,
      Integer studyTime,
      Integer focus,
      PublicScope scope,
      Member member,
      Place place) {
    return Post.builder()
        .title(title)
        .contents(contents)
        .startedAt(startedAt)
        .endedAt(endedAt)
        .studyDate(studyDate)
        .studyTime(studyTime)
        .focus(focus)
        .scope(scope)
        .member(member)
        .place(place)
        .build();
  }
}
