package com.plog.plogbackend.domain.map.service;

import com.plog.plogbackend.domain.map.implement.MapManager;
import com.plog.plogbackend.domain.map.model.MapPin;
import com.plog.plogbackend.domain.map.model.RecordSortType;
import com.plog.plogbackend.domain.map.model.Viewport;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MapService {
  private final MapManager mapManager;

  @Transactional(readOnly = true)
  public Slice<MapPin> findMyRecordPins(
      UUID memberKey, Viewport viewport, RecordSortType sortType, Cursorable<String> cursorable) {
    return mapManager.getRecordsPins(memberKey, viewport, sortType, cursorable);
  }

  @Transactional(readOnly = true)
  public Slice<MapPin> findMyBookmarkPins(
      UUID memberKey, Viewport viewport, RecordSortType sortType, Cursorable<String> cursorable) {
    return mapManager.getBookmarkPins(memberKey, viewport, sortType, cursorable);
  }
}
