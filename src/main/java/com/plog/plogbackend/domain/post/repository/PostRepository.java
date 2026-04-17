package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {}
