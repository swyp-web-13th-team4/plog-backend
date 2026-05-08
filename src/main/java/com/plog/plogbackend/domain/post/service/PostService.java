package com.plog.plogbackend.domain.post.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.badge.event.BadgeGrantEvent;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.domain.post.controller.dto.response.PostTextResponse;
import com.plog.plogbackend.domain.post.entity.PlaceCategory;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostTag;
import com.plog.plogbackend.domain.post.repository.PlaceCategoryRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.post.repository.PostTagRepository;
import com.plog.plogbackend.domain.post.service.dto.PlaceCommand;
import com.plog.plogbackend.domain.post.service.dto.PostCreateCommand;
import com.plog.plogbackend.domain.tag.Tag;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.domain.tag.repository.TagRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
  /** 첫 게시글 작성 뱃지 ID */
  private static final long BADGE_ID_FIRST_POST = 2L;

  /** 타임피커 최대 범위 레인지 */
  private static final int MAX_STUDY_MINUTES = 24 * 60;

  private static final int ACTIVITY_DAY_CUTOFF_HOURS = 6;

  private final MemberRepository memberRepository;
  private final TagRepository tagRepository;
  private final PlaceRepository placeRepository;
  private final PlaceCategoryRepository placeCategoryRepository;
  private final PostRepository postRepository;
  private final PostTagRepository postTagRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public PostTextResponse create(PostCreateCommand command) {

    validateTitleAndContext(command);

    LocalDateTime startedAt = mapStartedAt(command);
    LocalDateTime endedAt = mapEndedAt(command, startedAt);
    validateStudyDuration(startedAt, endedAt);

    Member member = findMember(command);
    PlaceCategory placeCategory = findPlaceCategory(command);
    List<Tag> findTags = validateAndFindTags(command);

    Place place = findOrCreatePlace(command);

    // Post 생성 및 저장 로직
    Post savedPost =
        postRepository.save(
            Post.of(
                command.title(),
                command.contents(),
                startedAt,
                endedAt,
                command.studyDate(),
                command.focus(),
                command.scope(),
                member,
                place,
                placeCategory));

    List<PostTag> postTags = findTags.stream().map(tag -> PostTag.of(savedPost, tag)).toList();
    postTagRepository.saveAll(postTags);

    // 첫 게시글 뱃지(id:2) 부여 이벤트 발행
    // - 트랜잭션 커밋 후 BadgeEventHandler가 독립 트랜잭션으로 처리
    long totalPosts = postRepository.countByMemberId(member.getId());
    if (totalPosts == 1) {
      eventPublisher.publishEvent(new BadgeGrantEvent(member.getId(), BADGE_ID_FIRST_POST));
    }

    return PostTextResponse.from(savedPost);
  }

  private Place findOrCreatePlace(PostCreateCommand command) {
    PlaceCommand placeReq = command.place();
    return placeRepository
        .findByNameAndAddress(placeReq.name(), placeReq.address())
        .orElseGet(
            () ->
                placeRepository.save(
                    Place.of(
                        placeReq.name(),
                        placeReq.address(),
                        placeReq.latitude(),
                        placeReq.longitude())));
  }

  private PlaceCategory findPlaceCategory(PostCreateCommand command) {
    return placeCategoryRepository
        .findByCategoryName(command.categoryCode())
        .orElseThrow(() -> new AppException(ErrorType.CATEGORY_NOT_FOUND));
  }

  private Member findMember(PostCreateCommand command) {
    return memberRepository
        .findByMemberKey(command.memberKey())
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));
  }

  private static void validateTitleAndContext(PostCreateCommand command) {
    // title text 입력
    int titleLength = command.title().trim().length();
    if (titleLength < 2 || titleLength > 20) {
      throw new AppException(ErrorType.INVALID_TITLE_LENGTH);
    }

    // contents는 텍스트 + 이모티콘 혼합 허용
    // codePointCount 함수는 이모티콘까지 한자리수로 인정하여 계산해준다
    String trimmedContents = command.contents().trim();
    int contentsCount = trimmedContents.codePointCount(0, trimmedContents.length());
    if (contentsCount < 20 || contentsCount > 200) {
      throw new AppException(ErrorType.INVALID_CONTENTS_LENGTH);
    }
  }

  private List<Tag> validateAndFindTags(PostCreateCommand command) {
    // 테그 입력값 검증필터
    // 테그는 5개를 초과할 수 없다
    List<PlaceTag> placeTags = command.placeTags().stream().distinct().toList();

    if (placeTags.size() > 5) {
      throw new AppException(ErrorType.TAG_LIMIT_EXCEEDED);
    }

    // 검색 로직
    List<Tag> findTags = tagRepository.findByPlaceTagIn(placeTags);

    // 전송된 데이터와 DB에 있는 테그 데이터가 정확한지 size로 검증한다
    if (placeTags.size() != findTags.size()) {
      throw new AppException(ErrorType.TAG_NOT_FOUND);
    }
    return findTags;
  }

  private LocalDateTime mapStartedAt(PostCreateCommand command) {
    return command.startedAt().atDate(command.studyDate());
  }

  /** 자정이 넘으면 자동으로 +1일 증가한다 */
  private LocalDateTime mapEndedAt(PostCreateCommand command, LocalDateTime startedAt) {
    LocalDateTime ended = command.endedAt().atDate(command.studyDate());

    if (ended.isAfter(startedAt)) {
      return ended; // 정상 케이스
    }

    // 종료 <= 시작인 경우, 자정 넘김인지 오타인지 판단
    Duration backwardGap = Duration.between(ended, startedAt);

    if (backwardGap.toHours() >= ACTIVITY_DAY_CUTOFF_HOURS) {
      // 오타로 판단 — 거부
      throw new AppException(ErrorType.INVALID_STUDY_TIME_RANGE);
    }

    // 자정 넘김으로 판단 — 보정
    return ended.plusDays(1);
  }

  private void validateStudyDuration(LocalDateTime startedAt, LocalDateTime endedAt) {
    long minutes = Duration.between(startedAt, endedAt).toMinutes();
    if (minutes > MAX_STUDY_MINUTES) {
      throw new AppException(ErrorType.STUDY_TIME_TOO_LONG);
    }
  }
}
