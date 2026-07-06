package com.plog.plogbackend.domain.inquiry.controller.dto.user;

import com.plog.plogbackend.domain.inquiry.contents.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateInquiryRequest(
    @NotNull(message = "필수 선택 항목이에요") Category category,
    @NotBlank(message = "필수 입력 항목이에요") String title,
    @NotBlank(message = "필수 입력 항목이에요") @Size(min = 1, max = 500, message = "필수 입력 항목이에요")
        String content,
    List<Long> deleteImageIds) {}
