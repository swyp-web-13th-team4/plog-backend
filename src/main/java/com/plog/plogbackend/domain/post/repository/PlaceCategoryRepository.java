package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.PlaceCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, Long> {

  Optional<PlaceCategory> findByName(String name);

  List<PlaceCategory> findByNameIn(List<String> names);
}
