package com.plog.plogbackend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.plog.plogbackend.domain.member.dto.response.MemberAnalyticsResponse;
import com.plog.plogbackend.domain.member.enums.WorkType;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.post.entity.PlaceCategory;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PublicScope;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@ActiveProfiles("test")
class MemberAnalyticsServiceTest {

  @Autowired private MemberAnalyticsService memberAnalyticsService;

  @MockitoBean private MemberRepository memberRepository;

  @Test
  @DisplayName("5개 이상의 게시글이 있지만 어떤 조건도 완벽히 충족하지 못할 때, 가장 근접한 유형을 반환한다")
  void getAnalytics_returnsClosestWorkType_whenNoStrictMatch() {
    // Given
    UUID memberKey = UUID.randomUUID();

    // 5개의 게시글 생성
    // 1. 오후 2시 시작 (로기, 포포 탈락)
    // 2. 시작 시간 편차 큼 (치치 탈락)
    // 3. 한 가지 카테고리만 방문 (하루 미충족, 하지만 점수는 -20으로 가장 높을 예정)
    // 4. 평균 작업 시간 150분 (토리 탈락)
    // 5. 집중도 보통 (나오 탈락)

    PlaceCategory category = mock(PlaceCategory.class);
    given(category.getId()).willReturn(1L);

    List<Post> posts = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      LocalDateTime start =
          LocalDateTime.of(2025, 1, 1 + i, 14, 0)
              .plusMinutes(i * 30); // 14:00, 14:30, 15:00, 15:30, 16:00
      LocalDateTime end = start.plusMinutes(150);

      Post post =
          Post.builder()
              .title("Test Post " + i)
              .startedAt(start)
              .endedAt(end)
              .studyTime(150)
              .focus(3)
              .scope(PublicScope.PUBLIC)
              .placeCategory(category)
              .build();

      // createdAt 설정 (최근 5회 정렬을 위함)
      ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.of(2025, 1, 1 + i, 16, 0));
      posts.add(post);
    }

    given(memberRepository.findMyPostsForAnalytics(memberKey)).willReturn(posts);

    // When
    MemberAnalyticsResponse response = memberAnalyticsService.getAnalytics(memberKey);

    // Then
    assertThat(response.totalPostCount()).isEqualTo(5);
    assertThat(response.workType()).isNotNull();

    // 점수 분석:
    // CHICHI: maxDev approx 60 mins -> -(60-30) = -30
    // LOGI: Morning Ratio 0% -> 0 - 60 = -60
    // HARU: Category count 1 -> (1 - 3) * 10 = -20
    // TORI: avg 150 -> 120 - 150 = -30
    // POPO: Night Ratio 0% -> 0 - 70 = -70
    // NAO: High focus count 0 -> -100
    // Max is -20 (HARU)

    assertThat(response.workType()).isEqualTo(WorkType.HARU);
  }
}
