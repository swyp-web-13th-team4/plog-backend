package com.plog.plogbackend.global.support;

import com.plog.plogbackend.domain.Member.entity.DefaultProfileImage;
import com.plog.plogbackend.domain.Member.repository.DefaultProfileImageRepository;
import com.plog.plogbackend.global.config.PlogProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 시작 시 {@code application.yml}의 기본 프로필 이미지 URL 목록을 {@code default_profile_image} 테이블에
 * 동기화합니다.
 *
 * <p>이미 존재하는 URL은 중복 삽입하지 않으므로 재시작해도 안전합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultProfileImageInitializer implements ApplicationRunner {

  private final DefaultProfileImageRepository defaultProfileImageRepository;
  private final PlogProperties plogProperties;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    List<String> urls = plogProperties.getDefaultProfileImages();
    if (urls == null || urls.isEmpty()) {
      log.warn("[DataInit] plog.default-profile-images 설정이 비어 있습니다. 기본 이미지를 삽입하지 않습니다.");
      return;
    }

    int inserted = 0;
    for (String url : urls) {
      if (!defaultProfileImageRepository.existsByImageUrl(url)) {
        defaultProfileImageRepository.save(DefaultProfileImage.of(url));
        inserted++;
      }
    }
    log.info("[DataInit] 기본 프로필 이미지 동기화 완료 - 총 {}개 중 {}개 신규 삽입", urls.size(), inserted);
  }
}
