package com.plog.plogbackend.domain.bookmark.entity;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.global.common.entity.BaseTimeStatusEntity;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;

@Entity
@RequiredArgsConstructor
public class BookMark extends BaseTimeStatusEntity {

  @Id @GeneratedValue private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  public BookMark(Member member, Post post) {
    this.member = member;
    this.post = post;
  }
}
