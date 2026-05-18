package com.plog.plogbackend.domain.member.repository;

import com.plog.plogbackend.domain.badge.entity.Badge;
import com.plog.plogbackend.domain.badge.entity.MemberBadge;
import com.plog.plogbackend.domain.badge.entity.QMemberBadge;
import com.plog.plogbackend.domain.bookmark.entity.QBookMark;
import com.plog.plogbackend.domain.member.QMember;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.QPost;
import com.plog.plogbackend.domain.post.entity.QPostTag;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  QMember member = QMember.member;
  QPost post = QPost.post;
  QBookMark bookMark = QBookMark.bookMark;
  QMemberBadge memberBadge = QMemberBadge.memberBadge;

  @Override
  public List<Post> findMyPostsSorted(UUID memberKey, String sort, List<PlaceTag> tags) {
    return queryFactory
        .selectFrom(post)
        .join(post.member, member)
        .fetchJoin()
        .leftJoin(post.place)
        .fetchJoin()
        .where(member.memberKey.eq(memberKey), tagsEqAll(tags))
        .orderBy(getPostOrderSpecifier(sort), post.id.desc())
        .fetch();
  }

  @Override
  public List<Post> findMyBookmarksSorted(UUID memberKey, String sort, List<PlaceTag> tags) {
    return queryFactory
        .select(post)
        .from(bookMark)
        .join(bookMark.post, post)
        .join(bookMark.member, member)
        .join(post.member)
        .fetchJoin()
        .leftJoin(post.place)
        .fetchJoin()
        .where(member.memberKey.eq(memberKey), tagsEqAll(tags))
        .orderBy(getBookmarkOrderSpecifier(sort), post.id.desc())
        .fetch();
  }

  private BooleanBuilder tagsEqAll(List<PlaceTag> tags) {
    BooleanBuilder builder = new BooleanBuilder();
    if (tags != null && !tags.isEmpty()) {
      for (PlaceTag placeTag : tags) {
        builder.and(post.tags.any().tag.placeTag.eq(placeTag));
      }
    }
    return builder;
  }

  private OrderSpecifier<?> getPostOrderSpecifier(String sort) {
    if (sort == null) return post.createdAt.desc();
    return switch (sort) {
      case "likes" -> post.likes.desc();
      case "focus" -> post.focus.desc();
      case "studyTime" -> post.studyTime.desc();
      case "latest" -> post.createdAt.desc();
      default -> post.createdAt.desc();
    };
  }

  private OrderSpecifier<?> getBookmarkOrderSpecifier(String sort) {
    if (sort == null) return post.createdAt.desc();
    return switch (sort) {
      case "likes" -> post.likes.desc();
      case "latest" -> post.createdAt.desc();
      default -> post.createdAt.desc();
    };
  }

  @Override
  public List<Badge> findMyBadges(UUID memberKey) {
    return queryFactory
        .select(memberBadge.badge)
        .from(memberBadge)
        .join(memberBadge.member, member)
        .where(member.memberKey.eq(memberKey))
        .orderBy(memberBadge.acquiredAt.desc())
        .fetch();
  }

  @Override
  public List<MemberBadge> findMemberBadgesByMemberKey(UUID memberKey) {
    return queryFactory
        .selectFrom(memberBadge)
        .join(memberBadge.member, member)
        .fetchJoin()
        .join(memberBadge.badge)
        .fetchJoin()
        .where(member.memberKey.eq(memberKey))
        .fetch();
  }

  @Override
  public Optional<MemberBadge> findMemberBadgeByBadgeId(UUID memberKey, Long badgeId) {
    return Optional.ofNullable(
        queryFactory
            .selectFrom(memberBadge)
            .join(memberBadge.member, member)
            .fetchJoin()
            .join(memberBadge.badge)
            .fetchJoin()
            .where(member.memberKey.eq(memberKey).and(memberBadge.badge.id.eq(badgeId)))
            .fetchOne());
  }

  @Override
  public boolean existsMemberBadge(UUID memberKey, Long badgeId) {
    Integer result =
        queryFactory
            .selectOne()
            .from(memberBadge)
            .join(memberBadge.member, member)
            .where(member.memberKey.eq(memberKey).and(memberBadge.badge.id.eq(badgeId)))
            .fetchFirst();
    return result != null;
  }

  /**
   * 분석용: 회원의 전체 게시글을 tags, placeCategory와 함께 조회합니다.
   *
   * <p>MultipleBagFetch 예외를 피하기 위해, Post ID 목록을 먼저 조회한 뒤 tags를 별도 쿼리로 초기화합니다.
   * placeCategory(OneToOne)는 N+1 방지를 위해 최종 쿼리에서 fetch join합니다.
   */
  @Override
  public List<Post> findMyPostsForAnalytics(UUID memberKey) {
    // 1단계: 해당 회원의 Post ID 목록을 조회
    List<Long> postIds =
        queryFactory
            .select(post.id)
            .from(post)
            .join(post.member, member)
            .where(member.memberKey.eq(memberKey))
            .fetch();

    if (postIds.isEmpty()) {
      return List.of();
    }

    // 2단계: Post를 tags와 함께 조회 (batch fetch)
    QPostTag postTag = QPostTag.postTag;
    queryFactory
        .selectFrom(post)
        .leftJoin(post.tags, postTag)
        .fetchJoin()
        .leftJoin(postTag.tag)
        .fetchJoin()
        .where(post.id.in(postIds))
        .fetch();

    // 3단계: Post를 placeCategory와 함께 조회 (N+1 방지 fetch join)
    return queryFactory
        .selectFrom(post)
        .distinct()
        .leftJoin(post.placeCategory)
        .fetchJoin()
        .where(post.id.in(postIds))
        .orderBy(post.createdAt.desc())
        .fetch();
  }
}
