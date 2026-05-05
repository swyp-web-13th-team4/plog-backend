package com.plog.plogbackend.domain.Member.repository;

import com.plog.plogbackend.domain.Member.entity.WorkTypeCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkTypeCardRepository extends JpaRepository<WorkTypeCard, Long> {
  List<WorkTypeCard> findAllByOrderByIdAsc();
}
