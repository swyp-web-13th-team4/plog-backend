package com.plog.plogbackend.domain.review.service;

import com.plog.plogbackend.domain.review.enums.ReviewEnvironmentName;
import com.plog.plogbackend.domain.review.model.PlaceReviewEnvironmentSummary;
import com.plog.plogbackend.domain.review.model.PlaceReviewSummary;
import com.plog.plogbackend.domain.review.repository.PlaceReviewStatisticsRepository;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewEnvironmentCount;
import com.plog.plogbackend.domain.review.repository.dto.PlaceReviewRatingSummary;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceReviewStatisticsService {

  private final PlaceReviewStatisticsRepository placeReviewStatisticsRepository;

  @Transactional(readOnly = true)
  public PlaceReviewSummary getSummary(Long placeId) {
    PlaceReviewRatingSummary ratingSummary =
        placeReviewStatisticsRepository.findRatingSummaryByPlaceId(placeId);

    if (ratingSummary == null || ratingSummary.reviewCount() == 0) {
      return PlaceReviewSummary.empty();
    }

    List<PlaceReviewEnvironmentSummary> environments =
        toEnvironmentSummaries(
            placeReviewStatisticsRepository.findEnvironmentCountsByPlaceId(placeId));

    return new PlaceReviewSummary(
        ratingSummary.reviewCount(), ratingSummary.averageRating(), environments);
  }

  private List<PlaceReviewEnvironmentSummary> toEnvironmentSummaries(
      List<PlaceReviewEnvironmentCount> counts) {
    Map<ReviewEnvironmentName, PlaceReviewEnvironmentCount> topByEnvironment =
        counts.stream()
            .collect(
                Collectors.toMap(
                    PlaceReviewEnvironmentCount::name, count -> count, this::pickMoreSelected));

    return topByEnvironment.values().stream()
        .map(count -> new PlaceReviewEnvironmentSummary(count.name(), count.score(), count.count()))
        .sorted(Comparator.comparing(summary -> summary.name().ordinal()))
        .toList();
  }

  private PlaceReviewEnvironmentCount pickMoreSelected(
      PlaceReviewEnvironmentCount left, PlaceReviewEnvironmentCount right) {
    int countCompare = left.count().compareTo(right.count());
    if (countCompare != 0) {
      return countCompare > 0 ? left : right;
    }

    return left.score() >= right.score() ? left : right;
  }
}
