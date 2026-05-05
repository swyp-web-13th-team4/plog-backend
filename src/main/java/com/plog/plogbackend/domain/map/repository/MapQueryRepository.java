package com.plog.plogbackend.domain.map.repository;

import static com.plog.plogbackend.domain.bookmark.entity.QBookMark.bookMark;
import static com.plog.plogbackend.domain.place.entity.QPlace.place;
import static com.plog.plogbackend.domain.post.entity.QPost.post;

import com.plog.plogbackend.domain.map.model.Viewport;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.QPostImage;
import com.plog.plogbackend.global.support.paging.Cursorable;
import com.plog.plogbackend.global.support.paging.Slice;
import com.plog.plogbackend.global.support.querydsl.QuerydslRepositorySupport;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MapQueryRepository extends QuerydslRepositorySupport {

  public MapQueryRepository() {
    super(Post.class);
  }

  public Slice<Tuple> findRecordPinsByMemberId(
      Long memberId, Viewport viewport, Cursorable<Long> cursorable) {
    QPostImage pi = new QPostImage("pi");
    QPostImage piInner = new QPostImage("piInner");
    List<Tuple> tuples =
        select(
                place.id,
                place.name,
                place.address,
                place.latitude,
                place.longitude,
                post.id.count(),
                post.studyTime.sum(),
                post.focus.avg(),
                JPAExpressions.select(pi.imageUrl)
                    .from(pi)
                    .where(
                        pi.post.place.id.eq(place.id),
                        pi.post.member.id.eq(memberId),
                        pi.id.eq(
                            JPAExpressions.select(piInner.id.max())
                                .from(piInner)
                                .where(
                                    piInner.post.place.id.eq(place.id),
                                    piInner.post.member.id.eq(memberId)))))
            .from(post)
            .join(post.place, place)
            .where(
                post.member.id.eq(memberId),
                place.latitude.between(viewport.getSwLat(), viewport.getNeLat()),
                place.longitude.between(viewport.getSwLng(), viewport.getNeLng()),
                ltPlaceId(cursorable.getCursor()))
            .groupBy(place.id)
            .orderBy(place.id.desc())
            .limit(cursorable.getLimit() + 1)
            .fetch();

    return new Slice<>(tuples, cursorable, hasNext(cursorable, tuples));
  }

  public Slice<Tuple> findBookmarkPinsByMemberId(
      Long memberId, Viewport viewport, Cursorable<Long> cursorable) {
    QPostImage pi = new QPostImage("pi");
    QPostImage piInner = new QPostImage("piInner");

    List<Tuple> tuples =
        select(
                place.id,
                place.name,
                place.address,
                place.latitude,
                place.longitude,
                bookMark.id.count(),
                post.studyTime.sum(),
                post.focus.avg(),
                JPAExpressions.select(pi.imageUrl)
                    .from(pi)
                    .where(
                        pi.post.place.id.eq(place.id),
                        pi.post.id.in(
                            JPAExpressions.select(bookMark.post.id)
                                .from(bookMark)
                                .where(bookMark.member.id.eq(memberId))),
                        pi.id.eq(
                            JPAExpressions.select(piInner.id.max())
                                .from(piInner)
                                .where(
                                    piInner.post.place.id.eq(place.id),
                                    piInner.post.id.in(
                                        JPAExpressions.select(bookMark.post.id)
                                            .from(bookMark)
                                            .where(bookMark.member.id.eq(memberId)))))))
            .from(bookMark)
            .join(bookMark.post, post)
            .join(post.place, place)
            .where(
                bookMark.member.id.eq(memberId),
                place.latitude.between(viewport.getSwLat(), viewport.getNeLat()),
                place.longitude.between(viewport.getSwLng(), viewport.getNeLng()),
                ltPlaceId(cursorable.getCursor()))
            .groupBy(place.id)
            .orderBy(place.id.desc())
            .limit(cursorable.getLimit() + 1)
            .fetch();

    return new Slice<>(tuples, cursorable, hasNext(cursorable, tuples));
  }

  private BooleanExpression ltPlaceId(Long cursor) {
    return cursor != null ? place.id.lt(cursor) : null;
  }
}
