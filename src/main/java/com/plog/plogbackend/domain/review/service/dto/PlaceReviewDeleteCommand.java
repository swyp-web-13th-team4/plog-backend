package com.plog.plogbackend.domain.review.service.dto;

import java.util.UUID;

public record PlaceReviewDeleteCommand(Long reviewId, UUID memberKey) {}
