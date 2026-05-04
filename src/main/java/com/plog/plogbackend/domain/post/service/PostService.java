package com.plog.plogbackend.domain.post.service;

import com.plog.plogbackend.domain.Member.Member;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.badge.event.BadgeGrantEvent;
import com.plog.plogbackend.domain.place.PlaceRepository;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.post.controller.dto.request.post.TimePickerRequest;
import com.plog.plogbackend.domain.post.controller.dto.response.PostTextResponse;
import com.plog.plogbackend.domain.post.entity.PlaceCategory;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostTag;
import com.plog.plogbackend.domain.post.repository.PlaceCategoryRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.domain.post.repository.PostTagRepository;
import com.plog.plogbackend.domain.post.service.dto.PostCreateCommand;
import com.plog.plogbackend.domain.tag.Tag;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import com.plog.plogbackend.domain.tag.repository.TagRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

  /** 첫 게시글 작성 뱃지 ID */
  private static final long BADGE_ID_FIRST_POST = 2L;

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
    List<Tag> findTags = validateTag(command);

    Member member =
        memberRepository
            .findByMemberKey(command.memberKey())
            .orElseThrow(() -> new AppException(ErrorType.MEMBER_NOT_FOUND));

    Place findPlace =
        placeRepository
            .findByName(command.placeName())
            .orElseThrow(() -> new AppException(ErrorType.PLACE_NOT_FOUND));

    List<String> filteredCategoryNames =
        command.categoryNames().stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .distinct()
            .toList();

    List<PlaceCategory> findCategories =
        placeCategoryRepository.findByNameIn(filteredCategoryNames);

    if (findCategories.size() != filteredCategoryNames.size()) {
      throw new AppException(ErrorType.CATEGORY_NOT_FOUND);
    }

    // 타임피커Dto -> LocalDateTime 맵핑 로직
    LocalDateTime mappedStartAt = mappedLocalDateTime(command.startedAt(), command);
    LocalDateTime mappedEndedAt = mappedLocalDateTime(command.endedAt(), command);

    // Post 생성 및 저장 로직
    Post post =
        Post.of(
            command.title(),
            command.contents(),
            mappedStartAt,
            mappedEndedAt,
            command.studyDate(),
            //studyTime은 계산하여 자동생성
            command.focus(),
            command.scope(),
            member,
            findPlace);
    Post savedPost = postRepository.save(post);

    List<PostTag> postTags = findTags.stream().map(tag -> PostTag.of(savedPost, tag)).toList();
    postTagRepository.saveAll(postTags);

/*
    List<PostCategory> postCategories =
        findCategories.stream().map(category -> PostCategory.of(savedPost, category)).toList();
    postCategoryRepository.saveAll(postCategories);

*/
    // 첫 게시글 뱃지(id:2) 부여 이벤트 발행
    // - 트랜잭션 커밋 후 BadgeEventHandler가 독립 트랜잭션으로 처리
    long totalPosts = postRepository.countByMemberId(member.getId());
    if (totalPosts == 1) {
      eventPublisher.publishEvent(new BadgeGrantEvent(member.getId(), BADGE_ID_FIRST_POST));
    }

    return PostTextResponse.from(savedPost);
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

  private List<Tag> validateTag(PostCreateCommand command) {
    // 테그 입력값 검증필터
    // 테그는 5개를 초과할 수 없다
    List<PlaceTag> placeTags = command.placeTags();
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

  private static LocalDateTime mappedLocalDateTime(
      TimePickerRequest request, PostCreateCommand command) {
    return LocalDateTime.of(command.studyDate(), LocalTime.of(request.hour(), request.minute()));
  }
}
