package com.plog.plogbackend.domain.Member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * 마이페이지 프로필 수정 요청 DTO.
 *
 * <p>닉네임과 프로필 이미지(파일 또는 기본 이미지 URL)를 한 번에 변경합니다.
 * 각 필드는 nullable이며, null인 경우 해당 항목은 변경하지 않습니다.
 */
@Schema(description = "프로필 수정 요청")
public record UpdateProfileRequest(

    @Schema(description = "변경할 닉네임 (null이면 변경 안 함, 2~20자)", example = "새닉네임")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    String nickname

    // 이미지는 MultipartFile / imageUrl 로 @RequestPart로 별도 수신
) {}
