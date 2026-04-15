package com.plog.plogbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PlogBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(PlogBackendApplication.class, args);
  }
}
