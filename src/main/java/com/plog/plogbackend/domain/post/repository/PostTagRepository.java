package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.PostTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

  @Modifying
  @Query("DELETE FROM PostTag pt WHERE pt.post.id IN :postIds")
  void deleteAllByPostIdIn(List<Long> postIds);
}
