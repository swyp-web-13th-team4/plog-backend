package com.plog.plogbackend.domain.place;

import com.plog.plogbackend.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByName(String name);
}
