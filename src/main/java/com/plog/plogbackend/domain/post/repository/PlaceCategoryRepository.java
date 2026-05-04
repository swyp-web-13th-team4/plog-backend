package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.PlaceCategory;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, Long> {

  Optional<PlaceCategory> findByCode(PlaceCategoryCode code);

  List<PlaceCategory> findByCodeIn(List<PlaceCategoryCode> codes);
}
