package com.plog.plogbackend.domain.review.service;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.review.entity.PlaceReview;
import com.plog.plogbackend.domain.review.repository.PlaceReviewRepository;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewCreateCommand;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewDeleteCommand;
import com.plog.plogbackend.domain.review.service.dto.PlaceReviewUpdateCommand;
import com.plog.plogbackend.global.common.Enum.EntityStatus;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceReviewService {

  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final PlaceReviewRepository placeReviewRepository;

  @Transactional
  public PlaceReview create(PlaceReviewCreateCommand command) {
    Member member = findMember(command.memberKey());
    Post post = findPost(command.postId());

    validatePostOwner(post, member);

    return placeReviewRepository
        .findByPostId(command.postId())
        .map(existingReview -> recreateOrThrow(existingReview, command))
        .orElseGet(() -> saveNewReview(post, member, command));
  }

  @Transactional
  public PlaceReview update(PlaceReviewUpdateCommand command) {
    PlaceReview placeReview =
        placeReviewRepository
            .findByIdAndStatus(command.reviewId(), EntityStatus.ACTIVE)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND));

    validateReviewOwner(placeReview, command.memberKey());
    placeReview.update(
        command.rating(), command.content(), command.environments(), LocalDateTime.now());
    return placeReview;
  }

  @Transactional
  public void delete(PlaceReviewDeleteCommand command) {
    PlaceReview placeReview =
        placeReviewRepository
            .findByIdAndStatus(command.reviewId(), EntityStatus.ACTIVE)
            .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND));

    validateReviewOwner(placeReview, command.memberKey());
    placeReview.delete();
  }

  private Member findMember(UUID memberKey) {
    return memberRepository
        .findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));
  }

  private Post findPost(Long postId) {
    return postRepository
        .findById(postId)
        .orElseThrow(() -> new AppException(ErrorType.POST_NOT_FOUND));
  }

  private void validatePostOwner(Post post, Member member) {
    if (!post.getMember().getId().equals(member.getId())) {
      throw new AppException(ErrorType.POST_FORBIDDEN);
    }
  }

  private void validateReviewOwner(PlaceReview placeReview, UUID memberKey) {
    if (!placeReview.getMember().getMemberKey().equals(memberKey)) {
      throw new AppException(ErrorType.POST_FORBIDDEN);
    }
  }

  private PlaceReview recreateOrThrow(
      PlaceReview existingReview, PlaceReviewCreateCommand command) {
    if (existingReview.getStatus() == EntityStatus.ACTIVE) {
      throw new AppException(ErrorType.PLACE_REVIEW_ALREADY_EXISTS);
    }

    existingReview.restore(
        command.rating(), command.content(), command.environments(), LocalDateTime.now());
    return existingReview;
  }

  private PlaceReview saveNewReview(Post post, Member member, PlaceReviewCreateCommand command) {
    return placeReviewRepository.save(
        PlaceReview.create(
            post, member, command.rating(), command.content(), command.environments()));
  }
}
