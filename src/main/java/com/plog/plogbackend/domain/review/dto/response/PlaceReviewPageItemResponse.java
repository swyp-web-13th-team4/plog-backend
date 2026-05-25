package com.plog.plogbackend.domain.review.dto.response;

import java.util.List;

public record PlaceReviewPageItemResponse(
        List<PlaceReviewListItemResponse> content,
        boolean hasNext,
        String nextCursor) {
    public static PlaceReviewPageItemResponse empty() {
        return new PlaceReviewPageItemResponse(List.of(), false, null);
    }
}
