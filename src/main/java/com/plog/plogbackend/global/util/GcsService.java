package com.plog.plogbackend.global.util;

import com.github.f4b6a3.uuid.UuidCreator;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

    String originalFilename = file.getOriginalFilename();
    String extension = extractSafeExtension(originalFilename);

    boolean isHeic = extension.equalsIgnoreCase(".heic") || extension.equalsIgnoreCase(".heif");

    // HEIC 파일인 경우 jpg로 변경
    if (isHeic) {
      extension = ".jpg";
    }

    String objectName = directory + "/" + UuidCreator.getTimeOrderedEpoch() + extension;
    String contentType = isHeic ? "image/jpeg" : file.getContentType();

    BlobInfo blobInfo =
        BlobInfo.newBuilder(BlobId.of(bucket, objectName)).setContentType(contentType).build();

    try (InputStream inputStream = file.getInputStream()) {
      if (isHeic) {
        log.info("HEIC 이미지를 JPEG로 변환 시작: {}", originalFilename);
        byte[] convertedBytes = convertHeicToJpeg(inputStream);
        storage.create(blobInfo, convertedBytes);
      } else {
        storage.createFrom(blobInfo, inputStream);
      }

      String finalUrl = GCS_BASE_URL + bucket + "/" + objectName;
      log.info("GCS 파일 업로드 완료. 생성된 URL: {}", finalUrl);
      return finalUrl;
    } catch (IOException e) {
      log.error("GCS 파일 업로드/변환 실패: {}", objectName, e);
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

  /**
   * HEIC/HEIF 이미지를 외부 프로세스(ImageMagick 또는 libheif)를 이용해 JPEG로 변환합니다. 자바 네이티브로는 특허 문제로 지원되지 않아 OS의
   * 모듈을 호출합니다.
   */
  private byte[] convertHeicToJpeg(InputStream inputStream) throws IOException {
    Path inputPath = Files.createTempFile("temp_heic_", ".heic");
    Path outputPath = Files.createTempFile("temp_jpeg_", ".jpg");

    try {
      Files.copy(inputStream, inputPath, StandardCopyOption.REPLACE_EXISTING);

      // 1순위: ImageMagick (magick)
      if (!executeCommand("magick", inputPath.toString(), outputPath.toString())) {
        // 2순위: ImageMagick legacy (convert)
        if (!executeCommand("convert", inputPath.toString(), outputPath.toString())) {
          // 3순위: libheif (heif-convert)
          if (!executeCommand("heif-convert", inputPath.toString(), outputPath.toString())) {
            log.error(
                "HEIC 변환 실패: ImageMagick(magick, convert) 및 libheif(heif-convert) 명령어를 찾을 수 없거나 실행에 실패했습니다.");
            throw new IOException("HEIC 변환 외부 모듈 호출 실패");
          }
        }
      }

      return Files.readAllBytes(outputPath);
    } finally {
      Files.deleteIfExists(inputPath);
      Files.deleteIfExists(outputPath);
    }
  }

  private boolean executeCommand(String... command) {
    try {
      ProcessBuilder pb = new ProcessBuilder(command);
      Process process = pb.start();
      return process.waitFor() == 0;
    } catch (Exception e) {
      return false;
    }
  }

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

  /**
   * 파일명에서 경로 인젝션을 방지하고 확장자만 안전하게 추출합니다.
   *
   * <p>경로 구분자(/ \)를 포함한 디렉터리 정보를 제거하고, 소문자 알파벳·숫자만 허용합니다.
   *
   * @param originalFilename 클라이언트에서 전달된 원본 파일명
   * @return ".jpg" 형태의 확장자, 유효하지 않으면 빈 문자열
   */
  private String extractSafeExtension(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      return "";
    }
    // 경로 구분자 이후 파일명만 추출 (경로 인젝션 방지)
    String basename = originalFilename.replaceAll(".*[/\\\\]", "");
    int dotIndex = basename.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == basename.length() - 1) {
      return "";
    }
    String ext = basename.substring(dotIndex + 1).toLowerCase();
    // 알파벳·숫자만 허용 (예: jpg, png, gif, webp)
    if (!ext.matches("[a-z0-9]+")) {
      log.warn("허용되지 않는 파일 확장자 - 확장자 제거 처리: {}", ext);
      return "";
    }
    return "." + ext;
  }
}
