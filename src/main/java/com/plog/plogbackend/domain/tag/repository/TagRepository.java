package com.plog.plogbackend.domain.tag.repository;

import com.plog.plogbackend.domain.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

  List<Tag> findByName(String name);

  List<Tag> findByNameIn(List<String> names);
}
