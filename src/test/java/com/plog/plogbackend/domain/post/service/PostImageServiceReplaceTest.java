package com.plog.plogbackend.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.domain.post.entity.PlaceCategory;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostImage;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.post.repository.PlaceCategoryRepository;
import com.plog.plogbackend.domain.post.repository.PostImageRepository;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import com.plog.plogbackend.global.util.GcsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Post.images 의 orphanRemoval=true 동기화 회귀 테스트. 이전에는 PostImageService.replacePostImages 가 컬렉션을 거치지
 * 않고 PostImageRepository.deleteAll/save 로 직접 자식을 조작해 영속성 컨텍스트와 DB 상태가 어긋났고, 트랜잭션 커밋 시
 * StaleStateException 으로 500 이 발생했다. 이 테스트는 동일 트랜잭션 경계에서 호출이 정상 종료되고 결과 컬렉션 상태가 의도대로 동기화되는지를 확인한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class PostImageServiceReplaceTest {

  @Autowired private PostImageService postImageService;
  @Autowired private PostRepository postRepository;
  @Autowired private PostImageRepository postImageRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private PlaceRepository placeRepository;
  @Autowired private PlaceCategoryRepository placeCategoryRepository;

  @MockitoBean private GcsService gcsService;

  @Test
  @Transactional
  @DisplayName("keep 2 + new 2 호출 시 orphanRemoval 충돌 없이 4건으로 동기화된다")
  void replacePostImages_keepsAndAdds_withoutStaleStateException() {
    Post post = seedPost("k1");
    List<PostImage> seeded = seedImages(post, 5);
    List<Long> keepIds = List.of(seeded.get(0).getId(), seeded.get(1).getId());

    AtomicInteger seq = new AtomicInteger();
    given(gcsService.upload(any(MultipartFile.class), anyString()))
        .willAnswer(inv -> "https://gcs.test/new-" + seq.getAndIncrement() + ".jpg");

    List<MultipartFile> newFiles =
        List.of(
            new MockMultipartFile("images", "a.jpg", "image/jpeg", new byte[] {1}),
            new MockMultipartFile("images", "b.jpg", "image/jpeg", new byte[] {2}));

    postImageService.replacePostImages(post.getId(), keepIds, newFiles);

    List<PostImage> after = post.getImages();
    assertThat(after).hasSize(4);
    assertThat(after).extracting(PostImage::getId).contains(keepIds.get(0), keepIds.get(1));
    assertThat(after)
        .extracting(PostImage::getImageUrl)
        .filteredOn(url -> url.startsWith("https://gcs.test/new-"))
        .hasSize(2);
  }

  @Test
  @Transactional
  @DisplayName("keep 0 + new 0 호출 시 모든 이미지가 orphanRemoval 로 정리된다")
  void replacePostImages_clearsAll_whenKeepEmptyAndNewEmpty() {
    Post post = seedPost("k2");
    seedImages(post, 3);

    postImageService.replacePostImages(post.getId(), List.of(), List.of());

    List<PostImage> after = post.getImages();
    assertThat(after).isEmpty();
  }

  // ---------- helpers ----------

  private Post seedPost(String providerSuffix) {
    Member member =
        memberRepository.save(
            Member.createNewMember(
                "nick-" + providerSuffix, "kakao-" + providerSuffix, null, null));
    Place place =
        placeRepository.save(
            Place.of("place-" + providerSuffix, "addr-" + providerSuffix, 0.0, 0.0));
    /*
        PlaceCategory category =
            placeCategoryRepository.save(
                PlaceCategory.builder().categoryName(PlaceCategoryCode.CAFE).build());
    */
    // 수정된 코드
    PlaceCategory category =
        placeCategoryRepository
            .findByCategoryName(PlaceCategoryCode.CAFE)
            .orElseGet(
                () ->
                    placeCategoryRepository.save(
                        PlaceCategory.builder().categoryName(PlaceCategoryCode.CAFE).build()));
    return postRepository.save(
        Post.of(
            "테스트 제목",
            "테스트 본문 내용입니다 충분한 길이를 갖도록 합니다 20자 이상",
            LocalDateTime.of(2025, 1, 1, 9, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0),
            LocalDate.of(2025, 1, 1),
            80,
            PublicScope.PUBLIC,
            member,
            place,
            category));
  }

  private List<PostImage> seedImages(Post post, int count) {
    return IntStream.range(0, count)
        .mapToObj(
            i -> {
              PostImage img = PostImage.of("https://seed.test/" + i + ".jpg", post);
              post.addImage(img);
              return postImageRepository.save(img);
            })
        .collect(Collectors.toList());
  }
}
