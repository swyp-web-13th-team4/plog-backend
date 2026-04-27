package com.plog.plogbackend.domain.Member.repository;

import com.plog.plogbackend.domain.badge.entity.Badge;
import com.plog.plogbackend.domain.post.entity.Post;
import java.util.List;
import java.util.UUID;

public interface MemberRepositoryCustom {
  List<Post> findMyPosts(UUID memberKey);

  List<Post> findMyBookmarks(UUID memberKey);

  List<Badge> findMyBadges(UUID memberKey);
}
