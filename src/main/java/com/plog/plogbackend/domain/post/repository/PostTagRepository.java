package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
}

