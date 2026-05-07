package com.plog.plogbackend.domain.map.controller.dto.request;

import com.plog.plogbackend.domain.map.model.RecordSortType;
import com.plog.plogbackend.domain.map.model.Viewport;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MapRequest(
    @NotNull @Min(-90) @Max(90) Double swLat,
    @NotNull @Min(-180) @Max(180) Double swLng,
    @NotNull @Min(-90) @Max(90) Double neLat,
    @NotNull @Min(-180) @Max(180) Double neLng,
    @NotNull RecordSortType sortType) {
  public Viewport toViewport() {
    return Viewport.of(swLat, swLng, neLat, neLng);
  }
}
