package com.plog.plogbackend.domain.inquiry.repository;

import com.plog.plogbackend.domain.inquiry.entity.Inquiry;
import com.plog.plogbackend.domain.member.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

  List<Inquiry> findByMember(Member member);
}
