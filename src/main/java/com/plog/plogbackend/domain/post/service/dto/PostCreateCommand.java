package com.plog.plogbackend.domain.post.service.dto;

import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostCreateCommand(
    String title,
    String contents,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    LocalDate studyDate,
    Integer studyTime,
    Integer focus,
    PublicScope scope,
    String placeName,
    List<PlaceTag> placeTags,
    List<String> categoryNames,
    UUID memberKey) {}
