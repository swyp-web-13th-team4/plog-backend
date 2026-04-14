package com.plog.plogbackend.domain.post.entity;

import com.plog.plogbackend.domain.tag.Tag;
import jakarta.persistence.*;

@Entity
public class PostTag {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id

    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
