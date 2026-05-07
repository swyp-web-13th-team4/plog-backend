package com.plog.plogbackend.domain.post.repository;

import com.plog.plogbackend.domain.post.entity.RecentPlaceSearch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RecentPlaceSearchRepository extends JpaRepository<RecentPlaceSearch, Long> {

  Optional<RecentPlaceSearch> findByMemberIdAndPlaceNameAndAddress(
      Long memberId, String placeName, String address);

  List<RecentPlaceSearch> findTop10ByMemberIdOrderBySearchedAtDesc(Long memberId);

  Optional<RecentPlaceSearch> findFirstByMemberIdOrderBySearchedAtAsc(Long memberId);

  long countByMemberId(Long memberId);

  @Modifying
  @Query("delete from RecentPlaceSearch r where r.id = :id and r.member.id = :memberId")
  int deleteByIdAndMemberId(Long id, Long memberId);

  @Modifying
  @Query("delete from RecentPlaceSearch r where r.member.id = :memberId")
  int deleteAllByMemberId(Long memberId);
}
