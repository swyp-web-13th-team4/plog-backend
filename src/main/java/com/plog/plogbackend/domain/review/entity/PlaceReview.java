package com.plog.plogbackend.domain.review.entity;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.global.common.Enum.EntityStatus;
import com.plog.plogbackend.global.common.entity.BaseTimeStatusEntity;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceReview extends BaseTimeStatusEntity {

  public static final int CONTENT_MAX_LENGTH = 300;
  private static final long EDITABLE_DAYS = 30;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false, unique = true)
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false)
  private Integer rating;

  @Column(length = CONTENT_MAX_LENGTH)
  private String content;

  @Column(nullable = false)
  private LocalDateTime editableUntil;

  @OneToMany(mappedBy = "placeReview", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PlaceReviewEnvironment> environments = new ArrayList<>();

  private PlaceReview(
      Post post,
      Member member,
      Integer rating,
      String content,
      Map<ReviewEnvironmentName, Integer> environments) {
    this.post = post;
    this.member = member;
    this.rating = rating;
    this.content = content;
    addEnvironments(environments);
  }

  public static PlaceReview create(
      Post post,
      Member member,
      Integer rating,
      String content,
      Map<ReviewEnvironmentName, Integer> environments) {
    return new PlaceReview(post, member, rating, content, environments);
  }

  @PrePersist
  private void setEditableUntil() {
    if (editableUntil == null) {
      editableUntil = LocalDateTime.now().plusDays(EDITABLE_DAYS);
    }
  }

  public boolean isEditable(LocalDateTime now) {
    return getStatus() == EntityStatus.ACTIVE && !now.isAfter(editableUntil);
  }

  public void validateEditable(LocalDateTime now) {
    if (!isEditable(now)) {
      throw new AppException(ErrorType.PLACE_REVIEW_EDIT_PERIOD_EXPIRED);
    }
  }

  public void update(
      Integer rating,
      String content,
      Map<ReviewEnvironmentName, Integer> environments,
      LocalDateTime now) {
    validateEditable(now);
    this.rating = rating;
    this.content = content;
    this.environments.clear();
    addEnvironments(environments);
  }

  public void delete() {
    deleteEntity();
  }

  public void restore(
      Integer rating,
      String content,
      Map<ReviewEnvironmentName, Integer> environments,
      LocalDateTime now) {
    restoreEntity();
    this.rating = rating;
    this.content = content;
    this.editableUntil = now.plusDays(EDITABLE_DAYS);
    this.environments.clear();
    addEnvironments(environments);
  }

  public Map<ReviewEnvironmentName, Integer> getEnvironments() {
    Map<ReviewEnvironmentName, Integer> environmentScores =
        new EnumMap<>(ReviewEnvironmentName.class);
    environments.forEach(
        environment -> environmentScores.put(environment.getName(), environment.getScore()));
    return environmentScores;
  }

  private void addEnvironments(Map<ReviewEnvironmentName, Integer> environments) {
    if (environments == null || environments.isEmpty()) {
      return;
    }

    environments.forEach(
        (name, score) -> {
          if (name != null && score != null) {
            this.environments.add(PlaceReviewEnvironment.of(this, name, score));
          }
        });
  }
}
