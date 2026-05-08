package com.plog.plogbackend.domain.Member.repository;

import com.plog.plogbackend.domain.Member.MemberAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MemberAgreementRepository extends JpaRepository<MemberAgreement, Long> {

  @Modifying
  @Query("DELETE FROM MemberAgreement ma WHERE ma.member.id = :memberId")
  void deleteAllByMemberId(Long memberId);
}
