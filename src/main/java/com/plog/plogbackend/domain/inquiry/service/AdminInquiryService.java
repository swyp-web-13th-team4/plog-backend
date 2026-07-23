package com.plog.plogbackend.domain.inquiry.service;

import com.plog.plogbackend.domain.inquiry.contents.Status;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminAnswerInquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminInquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AdminInquirysResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.AnswerInquiryRequest;
import com.plog.plogbackend.domain.inquiry.controller.dto.admin.InquiryStatusListResponse;
import com.plog.plogbackend.domain.inquiry.entity.Inquiry;
import com.plog.plogbackend.domain.inquiry.repository.InquiryRepository;
import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.enums.Role;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryService {

  private final MemberRepository memberRepository;
  private final InquiryRepository inquiryRepository;

  public List<AdminInquirysResponse> findInquirys(UUID memberKey) {

    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    if (!member.getRole().equals(Role.ADMIN)) {
      throw new AppException(ErrorType.REQUIRED_AUTH);
    }

    List<Inquiry> inquiries = inquiryRepository.findAll();

    List<AdminInquirysResponse> response = AdminInquirysResponse.from(inquiries);

    return response;
  }

  public InquiryStatusListResponse inquiryCount(UUID memberKey) {

    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    if (!member.getRole().equals(Role.ADMIN)) {
      throw new AppException(ErrorType.REQUIRED_AUTH);
    }

    List<Inquiry> inquiries = inquiryRepository.findAll();

    Map<Status, Long> collect =
        inquiries.stream()
            .collect(Collectors.groupingBy(Inquiry::getInquiryStatus, Collectors.counting()));
    long total = collect.size();
    long receiptCount = collect.getOrDefault(Status.RECEIPT, 0L);
    long waitCount = collect.getOrDefault(Status.WAIT, 0L);
    long finishCount = collect.getOrDefault(Status.FINISH, 0L);

    InquiryStatusListResponse counting =
        InquiryStatusListResponse.from(total, receiptCount, waitCount, finishCount);

    return counting;
  }

  public AdminInquiryResponse findInquiry(Long id, UUID memberKey) {

    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    if (!member.getRole().equals(Role.ADMIN)) {
      throw new AppException(ErrorType.REQUIRED_AUTH);
    }

    Inquiry inquiry =
        inquiryRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorType.INQUIRY_NOT_FOUND));

    AdminInquiryResponse response = AdminInquiryResponse.from(inquiry);

    return response;
  }

  @Transactional
  public AdminAnswerInquiryResponse createAnswer(
      Long id, AnswerInquiryRequest request, UUID memberKey) {

    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    if (!member.getRole().equals(Role.ADMIN)) {
      throw new AppException(ErrorType.REQUIRED_AUTH);
    }

    Inquiry inquiry =
        inquiryRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorType.INQUIRY_NOT_FOUND));
    inquiry.answer(request.category(), request.answerTitle(), request.answerContent());

    AdminAnswerInquiryResponse response = AdminAnswerInquiryResponse.from(inquiry);

    return response;
  }
}
