package com.plog.plogbackend.domain.place;

import com.plog.plogbackend.domain.place.entity.Place;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

  Optional<Place> findByName(String name);
}
