package com.plog.plogbackend.domain.place.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(
    name = "place",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_place_name_address",
            columnNames = {"name", "address"}))
public class Place {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  public static Place of(String name, String address, Double latitude, Double longitude) {
    Place place = new Place();
    place.name = name;
    place.address = address;
    place.latitude = latitude;
    place.longitude = longitude;
    return place;
  }
}
