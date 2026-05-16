package com.plog.plogbackend.domain.member.dto.response;

import lombok.Builder;

@Builder
public record TermsResponse(
    Long id, String name, String content, boolean required, String version) {}
