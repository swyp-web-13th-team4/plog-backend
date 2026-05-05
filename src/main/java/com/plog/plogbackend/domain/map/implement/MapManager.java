package com.plog.plogbackend.domain.map.implement;

import static com.plog.plogbackend.domain.bookmark.entity.QBookMark.bookMark;
import static com.plog.plogbackend.domain.place.entity.QPlace.place;
import static com.plog.plogbackend.domain.post.entity.QPost.post;

import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.map.model.MapPin;
import com.plog.plogbackend.domain.map.model.Viewport;
import com.plog.plogbackend.domain.map.repository.MapQueryRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import com.querydsl.core.Tuple;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapManager {
  private final MapQueryRepository mapQueryRepository;
  private final MemberRepository memberRepository;

  public Slice<MapPin> getRecordsPins(
      UUID memberKey, Viewport viewport, Cursorable<Long> cursorable) {

    Slice<Tuple> tupleSlice =
        mapQueryRepository.findRecordPinsByMemberId(getMemberId(memberKey), viewport, cursorable);

    return tupleSlice.map(
        tuple ->
            new MapPin(
                tuple.get(place.id),
                tuple.get(place.name),
                tuple.get(place.address),
                tuple.get(place.latitude),
                tuple.get(place.longitude),
                tuple.get(post.id.count()),
                tuple.get(post.studyTime.sum()),
                tuple.get(post.focus.avg()),
                tuple.get(8, String.class)));
  }

  public Slice<MapPin> getBookmarkPins(
      UUID memberKey, Viewport viewport, Cursorable<Long> cursorable) {
    Slice<Tuple> tupleSlice =
        mapQueryRepository.findBookmarkPinsByMemberId(getMemberId(memberKey), viewport, cursorable);

    return tupleSlice.map(
        tuple ->
            new MapPin(
                tuple.get(place.id),
                tuple.get(place.name),
                tuple.get(place.address),
                tuple.get(place.latitude),
                tuple.get(place.longitude),
                tuple.get(bookMark.id.count()),
                tuple.get(post.studyTime.sum()),
                tuple.get(post.focus.avg()),
                tuple.get(8, String.class)));
  }

  private Long getMemberId(UUID memberKey) {


    return memberRepository
        .findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND))
        .getId();

  }
}
