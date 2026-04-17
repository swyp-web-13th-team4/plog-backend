package com.plog.plogbackend.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Long id;

  @Column(nullable = false, length = 500)
  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @Builder
  private PostImage(String imageUrl, Post post) {
    this.imageUrl = imageUrl;
    this.post = post;
  }

  public static PostImage of(String imageUrl, Post post) {
    return PostImage.builder().imageUrl(imageUrl).post(post).build();
  }
}
