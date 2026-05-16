package com.plog.plogbackend.domain.member.repository;

import com.plog.plogbackend.domain.badge.entity.Badge;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import java.util.List;
import java.util.UUID;

public interface MemberRepositoryCustom {

  List<Post> findMyPostsSorted(UUID memberKey, String sort, List<PlaceTag> tags);

  List<Post> findMyBookmarksSorted(UUID memberKey, String sort, List<PlaceTag> tags);

  List<Badge> findMyBadges(UUID memberKey);

  boolean existsMemberBadge(UUID memberKey, Long badgeId);

  /** 분석용: 회원의 전체 게시글을 tags, categories 와 함께 조회합니다. */
  List<Post> findMyPostsForAnalytics(UUID memberKey);
}
