package com.plog.plogbackend.global.config;

import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "plog")
public class PlogProperties {

  /**
   * 기본 프로필 이미지 URL 목록.
   * application.yml의 plog.default-profile-images 에서 주입됩니다.
   */
  private List<String> defaultProfileImages = List.of();

  /** Set으로 변환해 O(1) 검사에 사용합니다. */
  public Set<String> getDefaultProfileImageSet() {
    return Set.copyOf(defaultProfileImages);
  }
}
