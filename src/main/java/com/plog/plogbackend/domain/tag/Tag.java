package com.plog.plogbackend.domain.tag;

import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Tag {

    @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

    @Enumerated(EnumType.STRING)
  private PlaceTag placeTag;
}
