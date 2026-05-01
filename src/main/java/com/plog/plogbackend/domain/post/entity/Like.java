package com.plog.plogbackend.domain.post.entity;

import com.plog.plogbackend.domain.Member.Member;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;

@Entity
@RequiredArgsConstructor
public class Like {

  @Id @GeneratedValue private Long id;

  @ManyToOne
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;

  public Like(Member member, Post post) {
    this.member = member;
    this.post = post;
  }
}
