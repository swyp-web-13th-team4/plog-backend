package com.plog.plogbackend.domain.Member.repository;

import com.plog.plogbackend.domain.Member.QMember;
import com.plog.plogbackend.domain.Member.entity.QMemberBadge;
import com.plog.plogbackend.domain.badge.entity.Badge;
import com.plog.plogbackend.domain.bookmark.entity.QBookMark;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.QPost;
import com.plog.plogbackend.domain.post.entity.QPostCategory;
import com.plog.plogbackend.domain.post.entity.QPostTag;
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
        .orderBy(post.createdAt.desc())
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
        .orderBy(bookMark.id.desc())
        .fetch();
  }

  @Override
  public List<Badge> findMyBadges(UUID memberKey) {
    return queryFactory
        .select(memberBadge.badge)
        .from(memberBadge)
        .join(memberBadge.member, member)
        .where(member.memberKey.eq(memberKey))
        .orderBy(memberBadge.createdAt.desc())
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

  /**
   * 분석용: 회원의 전체 게시글을 tags, categories 와 함께 조회합니다.
   *
   * <p>MultipleBagFetch 예외를 피하기 위해, Post ID 목록을 먼저 조회한 뒤
   * tags/categories를 별도 쿼리로 초기화합니다.
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
        .leftJoin(post.tags, postTag).fetchJoin()
        .leftJoin(postTag.tag).fetchJoin()
        .where(post.id.in(postIds))
        .fetch();

    // 3단계: Post를 categories와 함께 조회 (batch fetch)
    QPostCategory postCategory = QPostCategory.postCategory;
    return queryFactory
        .selectFrom(post)
        .distinct()
        .leftJoin(post.categories, postCategory).fetchJoin()
        .leftJoin(postCategory.placeCategory).fetchJoin()
        .where(post.id.in(postIds))
        .orderBy(post.createdAt.desc())
        .fetch();
  }
}

