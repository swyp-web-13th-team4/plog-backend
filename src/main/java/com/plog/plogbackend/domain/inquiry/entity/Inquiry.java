package com.plog.plogbackend.domain.inquiry.entity;

import com.plog.plogbackend.domain.inquiry.contents.Category;
import com.plog.plogbackend.domain.inquiry.contents.Status;
import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.global.common.entity.BaseTimeStatusEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseTimeStatusEntity {

  @Id @GeneratedValue private Long id;

  @Enumerated(EnumType.STRING)
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @Column(length = 20)
  private String title;

  @Column(length = 500)
  private String content;

  @Enumerated(EnumType.STRING)
  private Status inquiryStatus;

  private String answerTitle;
  private String answerContent;
  private LocalDateTime answerTime;

  @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InquiryImages> images = new ArrayList<>();

  public Inquiry(Category category, String title, String content, Member member) {
    this.category = category;
    this.title = title;
    this.content = content;
    this.member = member;
    this.inquiryStatus = Status.RECEIPT; // 초기 상태값
  }

  public void update(
      @NotBlank Category category, @NotBlank String title, @NotBlank String content) {

    this.category = category;
    this.title = title;
    this.content = content;
  }
}
