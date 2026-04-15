package com.plog.plogbackend.domain.post.entity;

import jakarta.persistence.*;

@Entity
public class PostImage {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Long id;

  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;
}
