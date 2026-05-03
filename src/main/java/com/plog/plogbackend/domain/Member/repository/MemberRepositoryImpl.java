package com.plog.plogbackend.domain.Member.repository;

import com.plog.plogbackend.domain.Member.QMember;
import com.plog.plogbackend.domain.badge.entity.Badge;
import com.plog.plogbackend.domain.badge.entity.QMemberBadge;
import com.plog.plogbackend.domain.bookmark.entity.QBookMark;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.QPost;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
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
  public List<Post> findMyPosts(UUID memberKey) {
    return queryFactory
        .selectFrom(post)
        .join(post.member, member)
        .fetchJoin()
        .leftJoin(post.place)
        .fetchJoin()
        .where(member.memberKey.eq(memberKey))
        .orderBy(post.createdAt.desc(), post.id.desc())
        .fetch();
  }

  @Override
  public List<Post> findMyBookmarks(UUID memberKey) {
    return queryFactory
        .select(post)
        .from(bookMark)
        .join(bookMark.post, post)
        .join(bookMark.member, member)
        .join(post.member)
        .fetchJoin() // 게시글 작성자 정보
        .leftJoin(post.place)
        .fetchJoin() // 게시글 장소 정보
        .where(member.memberKey.eq(memberKey))
        .orderBy(post.createdAt.desc(), post.id.desc())
        .fetch();
  }

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
}
