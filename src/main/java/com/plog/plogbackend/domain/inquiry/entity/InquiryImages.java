package com.plog.plogbackend.domain.inquiry.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryImages {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String imageUrl;

  // 중요: 어떤 문의글에 속한 이미지인지 부모 엔티티를 참조합니다.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inquiry_id")
  private Inquiry inquiry;

  // 생성에 필요한 정적 팩토리 메서드
  public static InquiryImages of(String imageUrl, Inquiry inquiry) {
    InquiryImages inquiryImage = new InquiryImages();
    inquiryImage.imageUrl = imageUrl;
    inquiryImage.inquiry = inquiry;
    return inquiryImage;
  }
}
