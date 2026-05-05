package com.plog.plogbackend.domain.map.controller.dto.response;

import com.plog.plogbackend.global.support.paging.Slice;
import java.util.List;

public record PageResponse<T>(List<T> content, boolean hasNext) {
  public static <T> PageResponse<T> of(Slice<T> slice) {
    return new PageResponse<>(slice.getContent(), slice.isHasNext());
  }
}
