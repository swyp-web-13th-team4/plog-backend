package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {

}
