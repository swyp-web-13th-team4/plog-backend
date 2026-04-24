package com.plog.plogbackend.domain.Member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 마이페이지 프로필 수정 요청 DTO.
 *
 * <p>닉네임과 소개글은 필수 입력 항목입니다. 이미지는 별도 파라미터로 수신하며 생략 가능합니다.
 */
@Schema(description = "프로필 수정 요청")
public record UpdateProfileRequest(
    @Schema(description = "변경할 닉네임 (필수, 2~10자)", example = "새닉네임")
        @NotBlank(message = "필수 입력 항목입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
        String nickname,
    @Schema(description = "소개글 (필수, 최대 100자)", example = "안녕하세요!")
        @NotBlank(message = "필수 입력 항목입니다.")
        @Size(max = 100, message = "소개글은 최대 100자까지 가능합니다.")
        String introduction

    // 이미지는 MultipartFile / defaultImageId 로 @RequestPart/@RequestParam으로 별도 수신
    ) {}

