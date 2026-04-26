package com.plog.plogbackend.domain.Member.controller;

import com.plog.plogbackend.domain.Member.dto.response.TermsResponse;
import com.plog.plogbackend.domain.Member.entity.Terms;
import com.plog.plogbackend.domain.Member.repository.TermsRepository;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "약관", description = "약관 관련 API")
@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsController {

  private final TermsRepository termsRepository;

  @Operation(summary = "약관 목록 조회", description = "회원가입 시 필요한 약관 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<List<TermsResponse>>> getTerms() {
    List<Terms> terms = termsRepository.findAll();
    List<TermsResponse> response =
        terms.stream()
            .map(
                t ->
                    TermsResponse.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .content(t.getContent())
                        .required(t.isRequired())
                        .version(t.getVersion())
                        .build())
            .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
