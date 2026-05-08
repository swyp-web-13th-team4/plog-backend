package com.plog.plogbackend.domain.map.service;

import com.plog.plogbackend.domain.map.implement.MapManager;
import com.plog.plogbackend.domain.map.model.MapCount;
import com.plog.plogbackend.domain.map.model.MapPin;
import com.plog.plogbackend.domain.map.model.PlaceRecord;
import com.plog.plogbackend.domain.map.model.PlaceSearchResult;
import com.plog.plogbackend.domain.map.model.PlaceSummary;
import com.plog.plogbackend.domain.map.model.SortType;
import com.plog.plogbackend.domain.map.model.Viewport;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import java.util.List;
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
  public List<MapPin> findMyRecordPins(UUID memberKey, Viewport viewport) {
    return mapManager.getRecordsPins(memberKey, viewport);
  }

  @Transactional(readOnly = true)
  public List<MapPin> findMyBookmarkPins(
      UUID memberKey, Viewport viewport) {
    return mapManager.getBookmarkPins(memberKey, viewport);
  }

  @Transactional(readOnly = true)
  public Slice<PlaceRecord> findPlaceRecords(
      UUID memberKey,
      Long placeId,
      SortType sortType,
      List<PlaceTag> tags,
      Cursorable<String> cursorable) {
    return mapManager.getPlaceRecords(memberKey, placeId, sortType, tags, cursorable);
  }

  @Transactional(readOnly = true)
  public Slice<PlaceSummary> findAllRecordPlaces(
      UUID memberKey, SortType sortType, Cursorable<String> cursorable) {
    return mapManager.getAllRecordPlaces(memberKey, sortType, cursorable);
  }

  @Transactional(readOnly = true)
  public Slice<PlaceSummary> findAllBookmarkPlaces(
      UUID memberKey, SortType sortType, Cursorable<String> cursorable) {
    return mapManager.getAllBookmarkPlaces(memberKey, sortType, cursorable);
  }

  @Transactional(readOnly = true)
  public List<PlaceSearchResult> searchRecordedPlaces(UUID memberKey, String keyword) {
    return mapManager.searchRecordedPlaces(memberKey, keyword);
  }

  @Transactional(readOnly = true)
  public Slice<PlaceRecord> findPlaceBookmarks(
      UUID memberKey,
      Long placeId,
      SortType sortType,
      List<PlaceTag> tags,
      Cursorable<String> cursorable) {
    return mapManager.getPlaceBookmarks(memberKey, placeId, sortType, tags, cursorable);
  }

  @Transactional(readOnly = true)
  public MapCount getMapCount(UUID memberKey) {
    return mapManager.getMapCount(memberKey);
  }
}
