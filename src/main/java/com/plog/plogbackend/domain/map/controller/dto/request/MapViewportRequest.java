package com.plog.plogbackend.domain.map.controller.dto.request;

import com.plog.plogbackend.domain.map.model.Viewport;
import jakarta.validation.constraints.NotNull;

public record MapViewportRequest(
    @NotNull Double swLat, @NotNull Double swLng, @NotNull Double neLat, @NotNull Double neLng) {
  public Viewport toViewport() {
    return Viewport.of(swLat, swLng, neLat, neLng);
  }
}
