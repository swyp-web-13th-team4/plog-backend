package com.plog.plogbackend.domain.bookmark.entity;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.post.entity.Post;
import jakarta.persistence.*;

@Entity
public class BookMark {

  @Id @GeneratedValue private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;
}
