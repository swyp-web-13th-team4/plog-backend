package com.plog.plogbackend.domain.member.controller;

import com.plog.plogbackend.domain.member.dto.response.TermsResponse;
import com.plog.plogbackend.domain.member.entity.Terms;
import com.plog.plogbackend.domain.member.repository.TermsRepository;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "약관", description = "약관 관련 API")
@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsController {

  private final TermsRepository termsRepository;

  @Operation(
      summary = "약관 단건 조회",
      description = "이름(isOver14, service, privacy, geolocation)을 통해 특정 약관을 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<TermsResponse>> getTerms(@RequestParam("name") String name) {
    Terms term =
        termsRepository
            .findByName(name)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관입니다: " + name));

    TermsResponse response =
        TermsResponse.builder()
            .id(term.getId())
            .name(term.getName())
            .content(term.getContent())
            .required(term.isRequired())
            .version(term.getVersion())
            .build();

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
