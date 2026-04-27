package com.plog.plogbackend.domain.Member.repository;

import com.plog.plogbackend.domain.Member.QMember;
import com.plog.plogbackend.domain.Member.entity.QMemberBadge;
import com.plog.plogbackend.domain.badge.entity.Badge;
import com.plog.plogbackend.domain.bookmark.entity.QBookMark;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.QPost;
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
}
