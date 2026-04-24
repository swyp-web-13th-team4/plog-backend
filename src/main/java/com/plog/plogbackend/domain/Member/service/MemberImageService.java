package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.dto.DefaultProfileImageDTO;
import com.plog.plogbackend.domain.Member.entity.DefaultProfileImage;
import com.plog.plogbackend.domain.Member.repository.DefaultProfileImageRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.util.GcsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberImageService {

  private static final String PROFILE_DIR = "profiles";

  private final GcsService gcsService;
  private final DefaultProfileImageRepository defaultProfileImageRepository;

  // ==========================================
  // 기본 이미지 조회 / 검증 (DB 기반)
  // ==========================================

  /** DB에 저장된 기본 프로필 이미지 목록(식별키와 URL 쌍)을 반환합니다. */
  @Transactional(readOnly = true)
  public List<DefaultProfileImageDTO> getDefaultProfileImages() {
    return defaultProfileImageRepository.findAllByOrderByIdAsc().stream()
        .map(
            entity ->
                DefaultProfileImageDTO.builder()
                    .id(entity.getId())
                    .imageUrl(entity.getImageUrl())
                    .build())
        .toList();
  }

  /** 전달된 ID가 DB에 등록된 기본 이미지인지 검증하고 URL을 반환합니다. */
  private String getDefaultImageUrl(Long defaultImageId) {
    DefaultProfileImage image =
        defaultProfileImageRepository
            .findById(defaultImageId)
            .orElseThrow(() -> new AppException(ErrorType.INVALID_DEFAULT_IMAGE_URL));
    return image.getImageUrl();
  }

  /** 주어진 URL이 사용자가 직접 업로드한 GCS 파일인지 판단합니다. (기본 이미지이면 false) */
  private boolean isCustomImage(String imageUrl) {
    if (imageUrl == null) return false;
    return !defaultProfileImageRepository.existsByImageUrl(imageUrl);
  }

  // ==========================================
  // 프로필 이미지 결정 (공통 로직)
  // ==========================================

  /**
   * 파일 또는 기본 이미지 ID로부터 최종 저장할 프로필 이미지 URL을 결정합니다.
   *
   * <ul>
   *   <li>파일이 있으면: GCS에 업로드하고 업로드된 URL을 반환
   *   <li>파일이 없고 defaultImageId가 있으면: DB에 등록된 기본 이미지인지 검증 후 해당 URL 반환
   *   <li>둘 다 없으면: FILE_EMPTY 에러
   * </ul>
   *
   * @param file 업로드할 이미지 파일 (nullable)
   * @param defaultImageId 기본 이미지 ID (file이 없을 때 사용, nullable)
   * @return 최종 프로필 이미지 URL
   */
  public String resolveProfileImageUrl(MultipartFile file, Long defaultImageId) {
    if (file != null && !file.isEmpty()) {
      String url = gcsService.upload(file, PROFILE_DIR);
      log.info("프로필 이미지 업로드 완료 - url: {}", url);
      return url;
    }
    if (defaultImageId != null) {
      return getDefaultImageUrl(defaultImageId);
    }
    throw new AppException(ErrorType.FILE_EMPTY);
  }

  // ==========================================
  // 회원가입 프로필 이미지 처리
  // ==========================================

  /**
   * 회원가입 시 프로필 이미지를 결정합니다. (공통 resolveProfileImageUrl 위임)
   *
   * @param profileImage 업로드 파일 (nullable)
   * @param defaultImageId 기본 이미지 ID (nullable)
   * @return 최종 프로필 이미지 URL
   */
  public String resolveSignupProfileImage(MultipartFile profileImage, Long defaultImageId) {
    return resolveProfileImageUrl(profileImage, defaultImageId);
  }

  // ==========================================
  // 프로필 이미지 변경 (내부 공통 처리)
  // ==========================================

  /**
   * 구 프로필 이미지를 GCS에서 삭제합니다 (커스텀 이미지인 경우에만, DB 커밋 이후 실행). 트랜잭션 밖에서 호출해야 하며, 이미 afterCommit 훅 내부에서
   * 호출됩니다.
   */
  private void scheduleOldImageDeletion(String oldImageUrl) {
    if (isCustomImage(oldImageUrl)) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              try {
                gcsService.delete(oldImageUrl);
                log.info("구 프로필 이미지 GCS 삭제 완료 - url: {}", oldImageUrl);
              } catch (Exception e) {
                log.warn("구 프로필 이미지 GCS 삭제 실패 - url: {}, error: {}", oldImageUrl, e.getMessage());
              }
            }
          });
    }
  }

  /**
   * 프로필 이미지 URL을 결정하고 구 이미지 삭제를 예약합니다. Member 엔티티 업데이트는 호출부(MemberService)에서 수행합니다.
   *
   * <p><b>GCS 삭제 순서 보장:</b><br>
   * DB 저장(커밋)을 먼저 완료한 뒤 구 파일을 GCS에서 삭제합니다.
   *
   * @param member 대상 회원 엔티티
   * @param file 업로드할 이미지 파일 (nullable)
   * @param defaultImageId 기본 이미지 ID (file이 없을 때 사용, nullable)
   * @return 새 프로필 이미지 URL (이미지를 변경하지 않는 경우 null)
   */
  public String resolveAndScheduleImageUpdate(
      Member member, MultipartFile file, Long defaultImageId) {
    // 둘 다 없으면 이미지 변경 없음
    if ((file == null || file.isEmpty()) && defaultImageId == null) {
      return null;
    }

    String oldImageUrl = member.getProfileImage();
    String newImageUrl = resolveProfileImageUrl(file, defaultImageId);

    scheduleOldImageDeletion(oldImageUrl);
    return newImageUrl;
  }
}
