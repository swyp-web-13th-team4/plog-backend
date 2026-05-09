package com.plog.plogbackend.domain.post.service.dto;

import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PostUpdateCommand(
    Long postId,
    UUID memberKey,
    String title,
    String contents,
    TimePickerCommand startedAt,
    TimePickerCommand endedAt,
    LocalDate studyDate,
    Integer focus,
    PublicScope scope,
    PlaceCommand place,
    List<PlaceTag> placeTags,
    PlaceCategoryCode categoryCode,
    List<Long> keepImageIds) {}
