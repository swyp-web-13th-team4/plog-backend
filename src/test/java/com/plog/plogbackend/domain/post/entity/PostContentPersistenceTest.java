package com.plog.plogbackend.domain.post.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.plog.plogbackend.domain.member.Member;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.place.entity.Place;
import com.plog.plogbackend.domain.place.repository.PlaceRepository;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.post.repository.PostRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostContentPersistenceTest {

  @Autowired private PostRepository postRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private PlaceRepository placeRepository;
  @Autowired private EntityManager em;

  @Test
  @DisplayName("도메인에서 허용하는 최대 길이 300자의 게시글 내용을 저장할 수 있다")
  void savePost_withMaxLengthContents() {
    Member member =
        memberRepository.save(Member.createNewMember("nick", "provider-contents", null, null));
    Place place = placeRepository.save(Place.of("place", "address", 37.0, 127.0));
    String contents = "a".repeat(Post.MAX_CONTENTS_COUNT);

    Post post =
        postRepository.save(
            Post.of(
                "테스트 제목",
                contents,
                LocalDateTime.of(2025, 1, 1, 9, 0),
                LocalDateTime.of(2025, 1, 1, 11, 0),
                LocalDate.of(2025, 1, 1),
                5,
                PublicScope.PUBLIC,
                member,
                place,
                PlaceCategoryCode.CAFE));

    em.flush();
    em.clear();

    assertThat(postRepository.findById(post.getId()).orElseThrow().getContents())
        .isEqualTo(contents);
  }
}
