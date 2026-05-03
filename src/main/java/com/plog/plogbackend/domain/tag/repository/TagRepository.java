package com.plog.plogbackend.domain.tag.repository;

import com.plog.plogbackend.domain.tag.Tag;
import java.util.List;

import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

  List<Tag> findByPlaceTag(PlaceTag placeTag);

  List<Tag> findByPlaceTagIn(List<PlaceTag> placeTag);
}
