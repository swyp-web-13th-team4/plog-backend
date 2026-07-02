package com.plog.plogbackend.domain.inquiry.repository;

import com.plog.plogbackend.domain.inquiry.entity.InquiryImages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryImageRepository extends JpaRepository<InquiryImages, Long> {

  void deleteByInquiryId(Long inquiryId);
}
