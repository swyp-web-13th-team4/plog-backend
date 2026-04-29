package com.plog.plogbackend.domain.badge.repository;

import com.plog.plogbackend.domain.badge.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {}
