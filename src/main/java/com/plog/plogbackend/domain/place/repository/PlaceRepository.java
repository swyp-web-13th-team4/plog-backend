package com.plog.plogbackend.domain.place.repository;

import com.plog.plogbackend.domain.place.entity.Place;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

  Optional<Place> findByNameAndAddress(String name, String address);
}
