package com.plog.plogbackend.global.util;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Google Cloud Storage 파일 업로드/삭제 공통 유틸 서비스.
 *
 * <p>GCS 객체 URL 형식: {@code https://storage.googleapis.com/{bucket}/{objectName}}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GcsService {

  private static final String GCS_BASE_URL = "https://storage.googleapis.com/";

  private final Storage storage;

  @Value("${spring.cloud.gcp.storage.bucket}")
  private String bucket;

  /**
   * 파일을 GCS에 업로드하고 공개 URL을 반환합니다.
   *
   * @param file 업로드할 파일
   * @param directory GCS 내 디렉터리 경로 (예: "profiles", "posts")
   * @return 업로드된 파일의 공개 접근 URL
   */
  public String upload(MultipartFile file, String directory) {
    validateFile(file);

    String objectName = buildObjectName(directory, file.getOriginalFilename());
    BlobInfo blobInfo =
        BlobInfo.newBuilder(BlobId.of(bucket, objectName))
            .setContentType(file.getContentType())
            .build();

    try {
      storage.create(blobInfo, file.getBytes());
      String finalUrl = GCS_BASE_URL + bucket + "/" + objectName;
      log.info("GCS 파일 업로드 완료. 생성된 URL: {}", finalUrl);
      return finalUrl;
    } catch (IOException e) {
      log.error("GCS 파일 업로드 실패: {}", objectName, e);
      throw new AppException(ErrorType.FILE_UPLOAD_FAILED);
    }
  }

  /**
   * GCS에서 파일을 삭제합니다. 파일이 존재하지 않아도 예외를 발생시키지 않습니다.
   *
   * @param fileUrl 삭제할 파일의 GCS 공개 URL
   */
  public void delete(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      return;
    }
    // 기본 이미지(GCS 외부 URL)는 삭제 시도하지 않음
    if (!fileUrl.startsWith(GCS_BASE_URL + bucket)) {
      log.debug("GCS 관할 외 URL이므로 삭제 생략: {}", fileUrl);
      return;
    }

    String objectName = fileUrl.replace(GCS_BASE_URL + bucket + "/", "");
    boolean deleted = storage.delete(BlobId.of(bucket, objectName));
    if (deleted) {
      log.debug("GCS 파일 삭제 완료: {}", objectName);
    } else {
      log.warn("GCS 파일이 존재하지 않아 삭제 생략: {}", objectName);
    }
  }

  // ==========================================
  // Private helpers
  // ==========================================

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new AppException(ErrorType.FILE_EMPTY);
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new AppException(ErrorType.FILE_TYPE_INVALID);
    }
    // 10MB 제한
    if (file.getSize() > 10 * 1024 * 1024) {
      throw new AppException(ErrorType.FILE_SIZE_EXCEEDED);
    }
  }

  /** 중복 없는 고유한 GCS 객체 이름을 생성합니다. */
  private String buildObjectName(String directory, String originalFilename) {
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    return directory + "/" + UUID.randomUUID() + extension;
  }
}
