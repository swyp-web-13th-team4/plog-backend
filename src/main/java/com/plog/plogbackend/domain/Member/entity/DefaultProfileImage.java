package com.plog.plogbackend.domain.Member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 선택 가능한 기본 프로필 이미지 목록을 DB에서 관리합니다. */
@Entity
@Table(name = "default_profile_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultProfileImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 1000, unique = true)
  private String imageUrl;

  @Builder
  private DefaultProfileImage(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public static DefaultProfileImage of(String imageUrl) {
    return DefaultProfileImage.builder().imageUrl(imageUrl).build();
  }
}
