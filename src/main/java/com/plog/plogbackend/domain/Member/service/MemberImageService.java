package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.image.dto.ImageUrlResponse;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.util.GcsService;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberImageService {

  private static final String PROFILE_DIR = "profiles";

  private final GcsService gcsService;
  private final MemberRepository memberRepository;

  private static final List<String> DEFAULT_IMAGES = List.of(
      "https://storage.googleapis.com/plog-bucket/profiles/default/profile1.png",
      "https://storage.googleapis.com/plog-bucket/profiles/default/profile2.png",
      "https://storage.googleapis.com/plog-bucket/profiles/default/profile3.png",
      "https://storage.googleapis.com/plog-bucket/profiles/default/profile4.png",
      "https://storage.googleapis.com/plog-bucket/profiles/default/profile5.png",
      "https://storage.googleapis.com/plog-bucket/profiles/default/profile6.png"
  );

  // ==========================================
  // 프로필 이미지
  // ==========================================

  public List<String> getDefaultProfileImages() {
    return DEFAULT_IMAGES;
  }

  /**
   * 회원가입 시 프로필 이미지를 GCS에 업로드하고 URL을 반환합니다. 최초 회원가입
   *
   * <p>이미지가 없으면 null을 반환 — Member 엔티티가 기본 이미지로 처리합니다.
   *
   * @param profileImage 업로드할 이미지 파일 (nullable)
   * @return GCS 업로드 URL, 이미지 없으면 null
   */
  public String uploadSignupProfileImage(MultipartFile profileImage) {
    if (profileImage == null || profileImage.isEmpty()) {
      return null;
    }
    String url = gcsService.upload(profileImage, PROFILE_DIR);
    log.info("회원가입 프로필 이미지 업로드 완료 - url: {}", url);
    return url;
  }

  /**
   * 프로필 이미지를 GCS에 업로드하고 Member 엔티티를 갱신합니다. 프로필 이미지 수정
   *
   * <p>기존 이미지가 GCS에 저장된 파일이면 먼저 삭제 후 새 이미지를 업로드합니다.
   *
   * @param memberKey 회원 UUID
   * @param file 업로드할 이미지 파일
   * @return 업로드된 이미지 공개 URL
   */
  @Transactional
  public ImageUrlResponse uploadProfileImage(UUID memberKey, MultipartFile file) {
    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    // 기존 프로필 이미지 삭제 (GCS 파일인 경우에만, 기본 이미지가 아닌 경우)
    String currentImage = member.getProfileImage();
    if (currentImage != null && !DEFAULT_IMAGES.contains(currentImage)) {
      gcsService.delete(currentImage);
    }

    String newImageUrl = gcsService.upload(file, PROFILE_DIR);
    member.updateProfileImage(newImageUrl);

    log.info("프로필 이미지 업로드 완료 - memberKey: {}, url: {}", memberKey, newImageUrl);
    return new ImageUrlResponse(newImageUrl);
  }



  /**
   * 선택한 기본 프로필 이미지 URL로 회원 프로필을 갱신합니다.
   *
   * @param memberKey 회원 UUID
   * @param imageUrl 선택한 이미지 URL
   */
  @Transactional
  public void updateProfileImageByUrl(UUID memberKey, String imageUrl) {
    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    // 기존 이미지가 GCS 업로드 파일이면 삭제 (기본 이미지가 아닌 경우)
    String currentImage = member.getProfileImage();
    if (currentImage != null && !DEFAULT_IMAGES.contains(currentImage)) {
      gcsService.delete(currentImage);
    }

    member.updateProfileImage(imageUrl);
    log.info("프로필 이미지 URL 변경 완료 - memberKey: {}, url: {}", memberKey, imageUrl);
  }
}
