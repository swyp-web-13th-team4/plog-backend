package com.plog.plogbackend.domain.post.entity;

import com.plog.plogbackend.domain.tag.entity.Tag;
import com.plog.plogbackend.global.common.entity.BaseTimeStatusEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PostTag extends BaseTimeStatusEntity {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tag_id")
  private Tag tag;

  @Builder
  private PostTag(Post post, Tag tag) {
      this.post = post;
      this.tag = tag;
  }

    public static PostTag of(Post post, Tag tag) {
        return PostTag.builder().post(post).tag(tag).build();
    }

}
