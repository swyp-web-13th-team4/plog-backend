package com.plog.plogbackend.domain.inquiry.service;

import com.plog.plogbackend.domain.inquiry.controller.dto.user.CreateInquiryRequest;
import com.plog.plogbackend.domain.inquiry.controller.dto.user.InquiryResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.user.InquirysResponse;
import com.plog.plogbackend.domain.inquiry.controller.dto.user.UpdateInquiryRequest;
import com.plog.plogbackend.domain.inquiry.entity.Inquiry;
import com.plog.plogbackend.domain.inquiry.entity.InquiryImages;
import com.plog.plogbackend.domain.inquiry.repository.InquiryImageRepository;
import com.plog.plogbackend.domain.inquiry.repository.InquiryRepository;
import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import com.plog.plogbackend.global.storage.CloudStorageService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class InquiryService {

  private final InquiryRepository inquiryRepository;
  private final MemberRepository memberRepository;
  private final CloudStorageService cloudStorageService;
  private final InquiryImageRepository inquiryImageRepository;

  @Transactional
  public long createInquiry(
      UUID memberKey, CreateInquiryRequest request, List<MultipartFile> images) {

    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));
    Inquiry inquiry = request.toEntity(member);
    inquiryRepository.save(inquiry);

    if (images != null && !images.isEmpty()) {
      for (MultipartFile file : images) {
        if (!file.isEmpty()) {
          String uploadUrl = cloudStorageService.upload(file, "inquiry");

          InquiryImages inquiryImage = InquiryImages.of(uploadUrl, inquiry);
          inquiryImageRepository.save(inquiryImage);
        }
      }
    }

    return inquiry.getId();
  }

  public List<InquirysResponse> findInquirys(UUID memberKey) {

    Member member =
        memberRepository
            .findByMemberKey(memberKey)
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    List<Inquiry> inquiries = inquiryRepository.findByMember(member);

    List<InquirysResponse> response = InquirysResponse.from(inquiries);
    return response;
  }

  public InquiryResponse findInquiry(Long id) {

    Inquiry inquiry =
        inquiryRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorType.INQUIRY_NOT_FOUND));
    InquiryResponse response = InquiryResponse.from(inquiry);

    return response;
  }

  @Transactional
  public void updateInquiry(
      Long id, UUID memberKey, @Valid UpdateInquiryRequest request, List<MultipartFile> images) {

    Inquiry inquiry =
        inquiryRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorType.INQUIRY_NOT_FOUND));

    if (!inquiry.getMember().getMemberKey().equals(memberKey)) {
      throw new AppException(ErrorType.INQUIRY_UNAUTHORIZED_ACCESS);
    }
    inquiry.update(request.category(), request.title(), request.content());

    if (request.deleteImageIds() != null && !request.deleteImageIds().isEmpty()) {
      inquiryImageRepository.deleteAllByIdInBatch(request.deleteImageIds());
    }
    if (images != null && !images.isEmpty()) {
      for (MultipartFile file : images) {
        String uploadUrl = cloudStorageService.upload(file, "unquiry");
        InquiryImages inquiryImage = InquiryImages.of(uploadUrl, inquiry);
        inquiryImageRepository.save(inquiryImage);
      }
    }
  }

  @Transactional
  public void deleteInquiry(Long id, UUID memberKey) {

    Inquiry inquiry =
        inquiryRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorType.INQUIRY_NOT_FOUND));

    if (!inquiry.getMember().getMemberKey().equals(memberKey)) {
      throw new AppException(ErrorType.INQUIRY_UNAUTHORIZED_ACCESS);
    }

    inquiryRepository.delete(inquiry);
  }
}
