package com.plog.plogbackend.domain.post.entity;

import com.plog.plogbackend.domain.BaseTimeStatusEntity;
import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
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


    @OneToMany(mappedBy = "post")
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostTag> tags = new ArrayList<>();


    @OneToMany(mappedBy = "post")
    private List<PostCategory> categories = new ArrayList<>();

}

