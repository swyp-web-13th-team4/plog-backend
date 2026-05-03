package com.plog.plogbackend.domain.Member.repository;

import com.plog.plogbackend.domain.Member.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {
  Optional<Terms> findByName(String name);
}
