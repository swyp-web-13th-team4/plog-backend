package com.plog.plogbackend.global.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 클라우드 스토리지 이미지 업로드/삭제 추상화 인터페이스.
 *
 * <p>비즈니스 코드(도메인 서비스)는 이 인터페이스에만 의존하며, 실제 클라우드 벤더(GCS, S3, Azure 등)의 구현체는 런타임에 주입됩니다.
 *
 * <h3>클라우드 벤더를 변경하는 방법</h3>
 *
 * <ol>
 *   <li>이 인터페이스를 구현하는 새 클래스를 생성합니다.
 *       <pre>{@code
 * @Service
 * public class S3CloudStorageService implements CloudStorageService {
 *     @Override
 *     public String upload(MultipartFile file, String directory) { ... }
 *     @Override
 *     public void delete(String fileUrl) { ... }
 * }
 *
 * }</pre>
 *   <li>기존 구현체(예: {@code GcsCloudStorageService})의 {@code @Service} 어노테이션을 제거하거나, {@code @Profile}
 *       어노테이션으로 환경별로 분기합니다.
 *       <pre>{@code
 * @Service
 * @Profile("gcs")
 * public class GcsCloudStorageService implements CloudStorageService { ... }
 *
 * @Service
 * @Profile("s3")
 * public class S3CloudStorageService implements CloudStorageService { ... }
 *
 * }</pre>
 *   <li>도메인 코드는 변경할 필요가 없습니다. 스프링이 활성 프로파일에 맞는 구현체를 자동으로 주입합니다.
 * </ol>
 *
 * <h4>현재 등록된 구현체</h4>
 *
 * <table>
 *   <tr><th>구현체</th><th>벤더</th><th>위치</th></tr>
 *   <tr><td>{@code GcsCloudStorageService}</td><td>Google Cloud Storage</td>
 *       <td>{@code global/storage/GcsCloudStorageService.java}</td></tr>
 * </table>
 *
 * <p>※ 새 구현체를 추가한 경우, 위 표에 행을 추가해 주세요.
 */
public interface CloudStorageService {

  /**
   * 파일을 클라우드 스토리지에 업로드하고 공개 URL을 반환합니다.
   *
   * @param file 업로드할 이미지 파일
   * @param directory 스토리지 내 디렉터리 경로 (예: "profiles", "posts")
   * @return 업로드된 파일의 공개 접근 URL
   */
  String upload(MultipartFile file, String directory);

  /**
   * 클라우드 스토리지에서 파일을 삭제합니다.
   *
   * <p>파일이 존재하지 않거나, 해당 스토리지 관할 외의 URL이면 무시합니다.
   *
   * @param fileUrl 삭제할 파일의 공개 URL
   */
  void delete(String fileUrl);
}
