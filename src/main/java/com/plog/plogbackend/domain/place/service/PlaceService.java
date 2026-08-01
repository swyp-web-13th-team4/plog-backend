package com.plog.plogbackend.domain.place.service;

import com.plog.plogbackend.domain.place.dto.response.PlaceNameResponse;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

  private final PlaceRepository placeRepository;

  public PlaceNameResponse getPlace(Long placeId) {
    Place place =
        placeRepository
            .findById(placeId)
            .orElseThrow(() -> new AppException(ErrorType.PLACE_NOT_FOUND));

    return PlaceNameResponse.from(place);
  }
}
