package com.plog.plogbackend.domain.post.service;

import static com.plog.plogbackend.domain.post.entity.Post.*;

import com.plog.plogbackend.domain.badge.event.BadgeGrantEvent;
import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.domain.post.controller.dto.response.PostTextResponse;
import com.plog.plogbackend.domain.post.entity.PlaceCategory;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostTag;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.post.repository.PlaceCategoryRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.post.repository.PostTagRepository;
import com.plog.plogbackend.domain.post.service.dto.PlaceCommand;
import com.plog.plogbackend.domain.post.service.dto.PostCreateCommand;
import com.plog.plogbackend.domain.post.service.dto.PostUpdateCommand;
import com.plog.plogbackend.domain.post.service.dto.TimePickerCommand;
import com.plog.plogbackend.domain.tag.Tag;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.domain.tag.repository.TagRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
    validateTitleAndContent(command.title(), command.contents());
    StudyTimeRange time =
        resolveStudyTime(command.startedAt(), command.endedAt(), command.studyDate());

    Member member = findMember(command.memberKey());
    PlaceCategory placeCategory = findPlaceCategory(command.categoryCode());
    List<Tag> findTags = validateAndFindTags(command.placeTags());
    Place place = findOrCreatePlace(command.place());

    Post savedPost =
        postRepository.save(
            of(
                command.title(),
                command.contents(),
                time.startedAt(),
                time.endedAt(),
                command.studyDate(),
                command.focus(),
                command.scope(),
                member,
                place,
                placeCategory));

    List<PostTag> postTags = findTags.stream().map(tag -> PostTag.of(savedPost, tag)).toList();
    postTagRepository.saveAll(postTags);

    long totalPosts = postRepository.countByMemberId(member.getId());
    if (totalPosts == 1) {
      eventPublisher.publishEvent(new BadgeGrantEvent(member.getId(), BADGE_ID_FIRST_POST));
    }

    return PostTextResponse.from(savedPost);
  }

  @Transactional
  public void update(PostUpdateCommand command) {
    Post post =
        postRepository
            .findById(command.postId())
            .orElseThrow(() -> new AppException(ErrorType.POST_NOT_FOUND));
    if (!post.getMember().getMemberKey().equals(command.memberKey())) {
      throw new AppException(ErrorType.POST_FORBIDDEN);
    }

    validateTitleAndContent(command.title(), command.contents());
    StudyTimeRange time =
        resolveStudyTime(command.startedAt(), command.endedAt(), command.studyDate());

    PlaceCategory placeCategory = findPlaceCategory(command.categoryCode());
    Place place = findOrCreatePlace(command.place());
    List<Tag> newTagEntities = validateAndFindTags(command.placeTags());

    post.update(
        command.title(),
        command.contents(),
        time.startedAt(),
        time.endedAt(),
        command.studyDate(),
        command.focus(),
        command.scope(),
        place,
        placeCategory);

    postTagRepository.deleteAllByPostId(post.getId());
    postTagRepository.flush();
    List<PostTag> newPostTagLinks =
        newTagEntities.stream().map(tag -> PostTag.of(post, tag)).toList();
    postTagRepository.saveAll(newPostTagLinks);

    // 양방향 컬렉션 동기화
    post.getTags().clear();
    post.getTags().addAll(newPostTagLinks);
  }

  @Transactional
  public void delete(Long postId, UUID memberKey) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new AppException(ErrorType.POST_NOT_FOUND));

    if (!post.getMember().getMemberKey().equals(memberKey)) {
      throw new AppException(ErrorType.POST_FORBIDDEN);
    }

    postTagRepository.deleteAllByPostId(post.getId());

    postRepository.delete(post);
  }

  private Member findMember(UUID memberKey) {
    return memberRepository
        .findByMemberKey(memberKey)
        .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));
  }

  private PlaceCategory findPlaceCategory(PlaceCategoryCode code) {
    return placeCategoryRepository
        .findByCategoryName(code)
        .orElseThrow(() -> new AppException(ErrorType.CATEGORY_NOT_FOUND));
  }

  private Place findOrCreatePlace(PlaceCommand placeCmd) {
    return placeRepository
        .findByNameAndAddress(placeCmd.name(), placeCmd.address())
        .orElseGet(
            () ->
                placeRepository.save(
                    Place.of(
                        placeCmd.name(),
                        placeCmd.address(),
                        placeCmd.latitude(),
                        placeCmd.longitude())));
  }

  private List<Tag> validateAndFindTags(List<PlaceTag> placeTagList) {
    List<PlaceTag> placeTags = placeTagList.stream().distinct().toList();
    if (placeTags.size() > 5) {
      throw new AppException(ErrorType.TAG_LIMIT_EXCEEDED);
    }
    List<Tag> findTags = tagRepository.findByPlaceTagIn(placeTags);
    if (placeTags.size() != findTags.size()) {
      throw new AppException(ErrorType.TAG_NOT_FOUND);
    }
    return findTags;
  }

  private static void validateTitleAndContent(String title, String contents) {
    int titleLength = title.trim().length();
    if (titleLength < MIN_TITLE_LENGTH || titleLength > MAX_TITLE_LENGTH) {
      throw new AppException(ErrorType.INVALID_TITLE_LENGTH);
    }
    String trimmedContents = contents.trim();
    int contentsCount = trimmedContents.codePointCount(0, trimmedContents.length());
    if (contentsCount < MIN_CONTENTS_COUNT || contentsCount > MAX_CONTENTS_COUNT) {
      throw new AppException(ErrorType.INVALID_CONTENTS_LENGTH);
    }
  }

  private StudyTimeRange resolveStudyTime(
      TimePickerCommand startedAt, TimePickerCommand endedAt, LocalDate studyDate) {
    LocalDateTime start = startedAt.atDate(studyDate);
    LocalDateTime end = adjustEndedAt(start, endedAt.atDate(studyDate));
    validateStudyDuration(start, end);
    return new StudyTimeRange(start, end);
  }

  private static LocalDateTime adjustEndedAt(LocalDateTime startedAt, LocalDateTime ended) {
    if (ended.isAfter(startedAt)) {
      return ended;
    }
    Duration backwardGap = Duration.between(ended, startedAt);
    if (backwardGap.toHours() >= ACTIVITY_DAY_CUTOFF_HOURS) {
      throw new AppException(ErrorType.INVALID_STUDY_TIME_RANGE);
    }
    return ended.plusDays(1);
  }

  private static void validateStudyDuration(LocalDateTime startedAt, LocalDateTime endedAt) {
    long minutes = Duration.between(startedAt, endedAt).toMinutes();
    if (minutes > MAX_STUDY_MINUTES) {
      throw new AppException(ErrorType.STUDY_TIME_TOO_LONG);
    }
  }

  private record StudyTimeRange(LocalDateTime startedAt, LocalDateTime endedAt) {}
}
