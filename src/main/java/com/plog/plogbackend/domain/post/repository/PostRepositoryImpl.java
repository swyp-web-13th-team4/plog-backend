package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.Member.QMember;
import com.plog.plogbackend.domain.bookmark.entity.QBookMark;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.post.entity.QPost;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  QMember member = QMember.member;
  QPost post = QPost.post;
  QBookMark bookMark = QBookMark.bookMark;

  // 날짜 지정 데이터보여주기
  @Override
  public List<Post> summary(PostSearch search) {

    return queryFactory
        .selectFrom(post)
        .join(post.member, member)
        .fetchJoin()
        .where(
            getBetween(search.getStartDate(), search.getEndDate()),
            memberIdEq(search.getMemberId()))
        .fetch();
  }

  @Override
  public Integer sum(Long memberId, PostSearch search) {
    return queryFactory
        .select(post.studyTime.sum())
        .from(post)
        .where(post.member.id.eq(memberId), getBetween(search.getStartDate(), search.getEndDate()))
        .fetchOne();
  }

  @Override
  public Long count(Long memberId, PostSearch search) {
    Long count =
        queryFactory
            .select(post.count())
            .from(post)
            .where(
                post.member.id.eq(memberId), getBetween(search.getStartDate(), search.getEndDate()))
            .fetchOne();

    return count;
  }

  @Override
  public List<Post> findAllByFeed(LocalDateTime lastStudyDate, Long lastPostId) {
    return queryFactory
        .selectFrom(post)
        .join(post.member)
        .fetchJoin()
        .join(post.place)
        .fetchJoin()
        .where(post.scope.eq(PublicScope.PUBLIC), scroll(lastStudyDate, lastPostId))
        .limit(10)
        .orderBy(post.createdAt.desc(), post.id.desc())
        .fetch();
  }

  @Override
  public List<Post> bookMarkPost(Long memberId) {
    return queryFactory
        .selectFrom(post)
        .join(bookMark)
        .fetchJoin()
        .on(bookMark.post.eq(post))
        .where(bookMark.member.id.eq(memberId))
        .fetch();
  }

  private BooleanExpression getBetween(LocalDate startDate, LocalDate endDate) {

    if (startDate == null || endDate == null) return null;

    return post.createdAt.between(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
  }

  private BooleanExpression scroll(LocalDateTime lastStudyDate, Long lastPostId) {

    if (lastStudyDate == null || lastPostId == null) return null;

    return post.createdAt
        .lt(lastStudyDate)
        .or(post.createdAt.eq(lastStudyDate).and(post.id.lt(lastPostId)));
  }

  private BooleanExpression memberIdEq(Long memberId) {
    return memberId != null ? member.id.eq(memberId) : null;
  }
}
