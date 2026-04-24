package com.plog.plogbackend.domain.Member.controller;

import com.plog.plogbackend.domain.Member.dto.DefaultProfileImageDTO;
import com.plog.plogbackend.domain.Member.dto.response.MyPageMemberResponse;
import com.plog.plogbackend.domain.Member.dto.request.UpdateProfileRequest;
import com.plog.plogbackend.domain.Member.service.MemberImageService;
import com.plog.plogbackend.domain.Member.service.MemberService;
import com.plog.plogbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "회원", description = "회원 정보 관련 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberImageService memberImageService;
  private final MemberService memberService;

  @Operation( // TODO : 회원 정보 조회 엔드포인트. 사용처 없으면 삭제
      summary = "회원 정보 조회",
      description = "로그인한 회원의 닉네임, 프로필 이미지 URL, 소개글 등을 조회합니다.")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<MyPageMemberResponse>> getMember(
      Authentication authentication) {
    UUID memberKey = (UUID) authentication.getPrincipal();
    MyPageMemberResponse response = memberService.getMyPageInfo(memberKey);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(
      summary = "기본 프로필 이미지 목록 조회",
      description = "DB에서 관리되는 선택 가능한 기본 프로필 이미지 목록(ID 및 URL)을 조회합니다.")
  @GetMapping("/default-images")
  public ResponseEntity<ApiResponse<List<DefaultProfileImageDTO>>> getDefaultProfileImages() {
    List<DefaultProfileImageDTO> defaultImages = memberImageService.getDefaultProfileImages();
    return ResponseEntity.ok(ApiResponse.success(defaultImages));
  }

  /**
   * 마이페이지 프로필 통합 수정 API.
   *
   * <p>닉네임, 소개글, 프로필 이미지(파일 or 기본 이미지 URL)를 한 번의 요청으로 변경합니다. - 닉네임이나 소개글이 null이면 해당 항목은 변경하지 않습니다.
   * - 이미지 파일(image)이 있으면 GCS 업로드 후 저장합니다. - 파일이 없고 imageUrl만 있으면 DB에 등록된 기본 이미지인지 검증 후 저장합니다. - 이미지
   * 관련 파라미터가 모두 없으면 이미지는 변경하지 않습니다.
   */
  @Operation(
      summary = "프로필 수정",
      description =
          """
          로그인한 회원의 프로필(닉네임, 소개글, 이미지)을 한 번에 변경합니다.
          - `request` 파트: 닉네임, 소개글 (multipart form-data 파라미터)
          - `image` 파트: 업로드할 이미지 파일 (선택)
          - `defaultImageId` 파트: 기본 이미지 ID (image가 없을 때 사용, 선택)
          - 이미지 파라미터가 모두 없으면 이미지는 그대로 유지됩니다.
          """)
  @PatchMapping(value = "/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<Void>> updateProfile(
      Authentication authentication,
      @Parameter(description = "프로필 변경 정보 (닉네임, 소개글)") @Valid @ModelAttribute
          UpdateProfileRequest request,
      @Parameter(description = "업로드할 이미지 파일 (jpg, png 등, 최대 10MB, 선택)")
          @RequestPart(value = "image", required = false)
          MultipartFile image,
      @Parameter(description = "선택한 기본 이미지 ID (image가 없을 때 사용, 선택)")
          @RequestParam(value = "defaultImageId", required = false)
          Long defaultImageId) {

    UUID memberKey = (UUID) authentication.getPrincipal();
    memberService.updateProfile(memberKey, request, image, defaultImageId);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @Operation(
      summary = "닉네임 유효성 검사",
      description = "닉네임 형식 및 중복 여부를 검사합니다. 실시간 디바운싱 검사용으로 사용할 수 있습니다.")
  @GetMapping("/validate/nickname")
  public ResponseEntity<ApiResponse<Void>> validateNickname(
      @Parameter(description = "검사할 닉네임") @RequestParam("nickname") String nickname,
      Authentication authentication) {
    UUID memberKey = null;
    if (authentication != null && authentication.getPrincipal() instanceof UUID) {
      memberKey = (UUID) authentication.getPrincipal();
    }
    memberService.validateNickname(nickname, memberKey);
    return ResponseEntity.ok(ApiResponse.success());
  }

  @Operation(
      summary = "소개글 유효성 검사",
      description = "소개글에 개인정보(연락처, 이메일) 및 SNS 계정이 포함되어 있는지 검사합니다.")
  @GetMapping("/validate/introduction")
  public ResponseEntity<ApiResponse<Void>> validateIntroduction(
      @Parameter(description = "검사할 소개글") @RequestParam("introduction") String introduction) {
    memberService.validateIntroduction(introduction);
    return ResponseEntity.ok(ApiResponse.success());
  }
}
