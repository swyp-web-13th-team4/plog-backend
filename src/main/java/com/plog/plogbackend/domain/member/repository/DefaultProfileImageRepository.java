package com.plog.plogbackend.domain.member.repository;

import com.plog.plogbackend.domain.member.entity.DefaultProfileImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefaultProfileImageRepository extends JpaRepository<DefaultProfileImage, Long> {

  List<DefaultProfileImage> findAllByOrderByIdAsc();

  boolean existsByImageUrl(String imageUrl);
}
