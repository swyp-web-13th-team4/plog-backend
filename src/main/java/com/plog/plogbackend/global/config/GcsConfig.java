package com.plog.plogbackend.global.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GCS 클라이언트 빈 설정.
 *
 * <p>로컬 환경: gcloud CLI의 ADC(Application Default Credentials)를 자동으로 사용하므로 별도 키 파일 불필요. GCP 환경(Cloud
 * Run, GCE 등): 서비스 계정 메타데이터를 ADC로 자동 인식하여 키 파일 불필요.
 */
@Configuration
public class GcsConfig {

  @Value("${spring.cloud.gcp.storage.project-id}")
  private String projectId;

  @Bean
  public Storage storage() {
    return StorageOptions.newBuilder().setProjectId(projectId).build().getService();
  }
}
