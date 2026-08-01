package com.plog.plogbackend.domain.review.service;

import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.domain.review.dto.response.PlaceReviewPlaceResponse;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceReviewPlaceService {

  private final PlaceRepository placeRepository;

  public PlaceReviewPlaceResponse getPlace(Long placeId) {
    Place place =
        placeRepository
            .findById(placeId)
            .orElseThrow(() -> new AppException(ErrorType.PLACE_NOT_FOUND));

    return PlaceReviewPlaceResponse.from(place);
  }
}
