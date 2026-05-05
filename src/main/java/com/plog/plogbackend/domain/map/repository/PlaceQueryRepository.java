package com.plog.plogbackend.domain.map.repository;

import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.global.support.querydsl.QuerydslRepositorySupport;

public class PlaceQueryRepository extends QuerydslRepositorySupport {

  public PlaceQueryRepository() {
    super(Place.class);
  }
}
