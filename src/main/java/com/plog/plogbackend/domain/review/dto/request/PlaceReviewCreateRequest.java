package com.plog.plogbackend.domain.review.dto.reqeust;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PlaceReviewCreateRequest(
        @NotNull @Min(1) @Max(5)
        Integer rating,

        @NotNull
        LocalDate visitedDate,

        LocalTime visitStartTime,
        LocalTime visitEndTime,

        List<String> environments,

        @Size(max = 1000)
        String content
) {}
