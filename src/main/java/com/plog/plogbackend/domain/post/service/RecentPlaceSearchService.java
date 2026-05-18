package com.plog.plogbackend.domain.post.service;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.post.controller.dto.response.RecentPlaceDeleteResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.RecentPlaceSaveResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.RecentPlaceSearchListResponse;
import com.plog.plogbackend.domain.post.controller.dto.response.RecentPlaceSearchResponse;
import com.plog.plogbackend.domain.post.entity.RecentPlaceSearch;
import com.plog.plogbackend.domain.post.repository.RecentPlaceSearchRepository;
import com.plog.plogbackend.domain.post.service.dto.RecentPlaceSaveCommand;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentPlaceSearchService {

  private static final int MAX_PER_MEMBER = 10;

  private final RecentPlaceSearchRepository recentSearchRepo;
  private final MemberRepository memberRepository;

  public RecentPlaceSearchListResponse findRecent(UUID memberKey) {
    Long memberId = resolveMemberId(memberKey);
    List<RecentPlaceSearchResponse> resultList =
        recentSearchRepo.findTop10ByMemberIdOrderBySearchedAtDesc(memberId).stream()
            .map(RecentPlaceSearchResponse::from)
            .toList();

    return RecentPlaceSearchListResponse.of(resultList);
  }

  @Transactional
  public RecentPlaceSaveResponse save(UUID memberKey, RecentPlaceSaveCommand cmd) {
    Member member = findMemberOrThrow(memberKey);
    upsert(member, cmd);
    return RecentPlaceSaveResponse.of((int) recentSearchRepo.countByMemberId(member.getId()));
  }

  @Transactional
  public RecentPlaceDeleteResponse deleteOne(Long id, UUID memberKey) {
    Long memberId = resolveMemberId(memberKey);
    int affected = recentSearchRepo.deleteByIdAndMemberId(id, memberId);
    if (affected == 0) {
      // 없는 id이거나 다른 사람 것 → 보안상 동일 메시지로 통합
      throw new AppException(ErrorType.RECENT_SEARCH_NOT_FOUND);
    }
    return new RecentPlaceDeleteResponse(affected);
  }

  @Transactional
  public RecentPlaceDeleteResponse deleteAll(UUID memberKey) {
    int deletedCount = recentSearchRepo.deleteAllByMemberId(resolveMemberId(memberKey));
    return new RecentPlaceDeleteResponse(deletedCount);
  }

  private Long resolveMemberId(UUID memberKey) {
    return memberRepository
        .findByMemberKey(memberKey)
        .map(Member::getId)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));
  }

  private void upsert(Member member, RecentPlaceSaveCommand cmd) {
    try {
      recentSearchRepo
          .findByMemberIdAndPlaceNameAndAddress(member.getId(), cmd.placeName(), cmd.address())
          .ifPresentOrElse(
              RecentPlaceSearch::touch,
              () -> {
                recentSearchRepo.save(
                    RecentPlaceSearch.of(
                        member, cmd.placeName(), cmd.address(), cmd.latitude(), cmd.longitude()));
                prune(member.getId());
              });
    } catch (DataIntegrityViolationException e) {
      recentSearchRepo
          .findByMemberIdAndPlaceNameAndAddress(member.getId(), cmd.placeName(), cmd.address())
          .ifPresent(RecentPlaceSearch::touch);
    }
  }

  private Member findMemberOrThrow(UUID memberKey) {
    return memberRepository
        .findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));
  }

  private void prune(Long memberId) {
    if (recentSearchRepo.countByMemberId(memberId) > MAX_PER_MEMBER) {
      recentSearchRepo
          .findFirstByMemberIdOrderBySearchedAtAsc(memberId)
          .ifPresent(recentSearchRepo::delete);
    }
  }
}
