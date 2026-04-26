package com.plog.plogbackend.domain.Member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record MemberSignupRequest(
    @Schema(description = "닉네임 (필수, 2~10자)")
        @NotBlank(message = "필수 입력 항목입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
        String nickname,
    @Schema(description = "소개글 (필수, 최대 100자)")
        @NotBlank(message = "필수 입력 항목입니다.")
        @Size(max = 100, message = "소개글은 최대 100자까지 가능합니다.")
        String introduction,
    @Schema(description = "약관 동의 여부") Map<Long, Boolean> termsAgreements) {}
