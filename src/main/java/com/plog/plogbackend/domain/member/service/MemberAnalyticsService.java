package com.plog.plogbackend.domain.member.service;

import com.plog.plogbackend.domain.member.dto.response.FocusEnvironmentResponse;
import com.plog.plogbackend.domain.member.dto.response.MemberAnalyticsResponse;
import com.plog.plogbackend.domain.member.dto.response.SpaceRankingResponse;
import com.plog.plogbackend.domain.member.enums.WorkType;
import com.plog.plogbackend.domain.member.repository.MemberRepository;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostTag;
import com.plog.plogbackend.domain.post.enums.PlaceCategoryCode;
import com.plog.plogbackend.domain.tag.Tag;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 분석 정보를 계산하는 서비스.
 *
 * <p>MyPageService에서 위임받아 5가지 분석 결과를 생성합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAnalyticsService {

  private final MemberRepository memberRepository;
  private static final int WORK_TYPE_MIN_POSTS = 5; // 분석 카드 최소 게시글 개수(이상)
  private static final int FOCUS_ENV_MIN_POSTS = 15; // 몰입 환경 분석 최소 게시글 개수(이상)

  /**
   * 회원의 전체 분석 정보를 조회합니다.
   *
   * @param memberKey 회원 UUID
   * @return 5가지 분석 결과
   */
  @Transactional(readOnly = true)
  public MemberAnalyticsResponse getAnalytics(UUID memberKey) { // I/O 통합 매서드
    List<Post> posts = memberRepository.findMyPostsForAnalytics(memberKey);
    int totalCount = posts.size();

    log.info(
        "[MemberAnalytics] Starting analytics for memberKey: {}, total posts: {}",
        memberKey,
        totalCount);

    int totalStudyTime =
        posts.stream().filter(p -> p.getStudyTime() != null).mapToInt(Post::getStudyTime).sum();

    WorkType workType = totalCount >= WORK_TYPE_MIN_POSTS ? analyzeWorkType(posts) : null;

    FocusEnvironmentResponse focusEnv =
        totalCount >= FOCUS_ENV_MIN_POSTS ? analyzeFocusEnvironment(posts) : null;

    List<SpaceRankingResponse> spaceRankings =
        totalCount >= FOCUS_ENV_MIN_POSTS ? analyzeSpaceRanking(posts) : null;

    return new MemberAnalyticsResponse(
        totalCount, totalStudyTime, workType, focusEnv, spaceRankings);
  }

  // ========== 작업 유형 카드 분석 ==========

  private WorkType analyzeWorkType(List<Post> posts) {
    Map<WorkType, Double> scores = new HashMap<>();

    List<Post> validStartedAtPosts = posts.stream().filter(p -> p.getStartedAt() != null).toList();
    List<Post> validStudyTimePosts = posts.stream().filter(p -> p.getStudyTime() != null).toList();

    scores.put(WorkType.CHICHI, calcType1Score(validStartedAtPosts));
    scores.put(WorkType.LOGI, calcType2Score(validStartedAtPosts));
    scores.put(WorkType.HARU, calcType3Score(posts));
    scores.put(WorkType.TORI, calcType4Score(validStudyTimePosts));
    scores.put(WorkType.POPO, calcType5Score(validStartedAtPosts));
    scores.put(WorkType.NAO, calcType6Score(posts));

    log.info("[MemberAnalytics] WorkType Scores: {}", scores);

    // 1단계: 조건 충족(양수 점수)인 유형 중 최고 점수 선택
    WorkType bestType =
        scores.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);

    // 2단계: 조건 충족 유형이 없으면, 가장 점수가 높은(가장 근접한) 유형 선택
    if (bestType == null) {
      bestType =
          scores.entrySet().stream()
              .max(Map.Entry.comparingByValue())
              .map(Map.Entry::getKey)
              .orElse(WorkType.LOGI); // 최후의 보루
      log.info("[MemberAnalytics] No strict match found. Selected closest WorkType: {}", bestType);
    } else {
      log.info("[MemberAnalytics] Selected WorkType: {}", bestType);
    }

    return bestType;
  }

  /** 유형 1: 성실 루틴형 치치 (시간의 규칙성) - 최근 5회 시작 시간 오차 ±30분 이내 */
  private double calcType1Score(List<Post> postsWithStartedAt) {
    List<Post> recent5 = getRecent(postsWithStartedAt, 5);
    if (recent5.size() < 5) return -100;

    List<LocalTime> times = recent5.stream().map(p -> p.getStartedAt().toLocalTime()).toList();

    // 시간은 원형(Circular) 데이터이므로 삼각함수를 이용하여 평균을 계산합니다.
    double sumSin = 0;
    double sumCos = 0;
    for (LocalTime t : times) {
      double radians = (t.getHour() * 60 + t.getMinute()) * 2 * Math.PI / 1440.0;
      sumSin += Math.sin(radians);
      sumCos += Math.cos(radians);
    }
    double avgRadians = Math.atan2(sumSin / times.size(), sumCos / times.size());
    if (avgRadians < 0) avgRadians += 2 * Math.PI;
    double avgMinutes = avgRadians * 1440.0 / (2 * Math.PI);

    // [개선] 평균 편차(avgDeviation) → 최대 편차(maxDeviation)로 교체
    // 기획서 요구사항: '시작 시간 오차가 ±30분 이내로 규칙적인 경우'
    // 하루라도 30분을 초과하면 조건을 불충족해야 하므로 최대 편차로 검증합니다.
    double maxDeviation =
        times.stream()
            .mapToDouble(
                t -> {
                  double diff = Math.abs((t.getHour() * 60 + t.getMinute()) - avgMinutes);
                  return Math.min(diff, 1440 - diff);
                })
            .max()
            .orElse(31);

    if (maxDeviation > 30) return -(maxDeviation - 30); // 단 하나의 기록이라도 ±30분을 초과하면 미충족 (음수 거리 반환)
    return Math.max(0, 100 - (maxDeviation * 1.66));
  }

  /** 유형 2: 부지런한 아침형 로기 (오전 집중형) - 오전(06:00~10:50) 시작 비중 60% 이상 */
  private double calcType2Score(List<Post> postsWithStartedAt) {
    long total = postsWithStartedAt.size();
    if (total == 0) return -100;

    long morningCount =
        postsWithStartedAt.stream()
            .filter(p -> "오전".equals(classifyTime(p.getStartedAt().toLocalTime())))
            .count();

    double ratio = (morningCount * 100.0) / total;
    if (ratio < 60) return ratio - 60; // 부족한 비율만큼 음수 반환
    return Math.min(100, 50 + ((ratio - 60) * 1.25));
  }

  /** 유형 3: 자유로운 탐험형 하루 (장소 유목민형) - 최근 5회 중 PlaceCategory 3곳 이상 */
  private double calcType3Score(List<Post> posts) {
    List<Post> recent5 = getRecent(posts, 5);
    if (recent5.size() < 5) return -100;

    Set<PlaceCategoryCode> placeCategories =
        recent5.stream()
            .map(Post::getPlaceCategory)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    int count = placeCategories.size();
    if (count < 3) return (count - 3) * 10.0; // 부족한 개수만큼 감점
    return Math.min(100, 50 + ((count - 3) * 25.0));
  }

  /** 유형 4: 빠른 스퍼트형 토리 (단기 몰입형) - 평균 작업 120분 미만 & 2시간 미만 빈도 60% 이상 */
  private double calcType4Score(List<Post> postsWithStudyTime) {
    if (postsWithStudyTime.isEmpty()) return -100;

    double avg = postsWithStudyTime.stream().mapToInt(Post::getStudyTime).average().orElse(120);
    long shortCount = postsWithStudyTime.stream().filter(p -> p.getStudyTime() < 120).count();
    double shortRatio = (shortCount * 100.0) / postsWithStudyTime.size();

    // 두 조건 중 더 많이 부족한 쪽을 기준으로 반환 (0 이하면 미충족)
    double avgDiff = 120 - avg;
    double ratioDiff = shortRatio - 60;

    if (avgDiff <= 0 || ratioDiff < 0) {
      return Math.min(avgDiff, ratioDiff);
    }

    return Math.min(100, 100 - ((avg - 60) * 0.83));
  }

  /** 유형 5: 고요한 새벽형 포포 (올빼미형) - 밤(20:00~23:00) 시작 비중 70% 이상 */
  private double calcType5Score(List<Post> postsWithStartedAt) {
    long total = postsWithStartedAt.size();
    if (total == 0) return -100;

    long nightCount =
        postsWithStartedAt.stream()
            .filter(p -> "밤".equals(classifyTime(p.getStartedAt().toLocalTime())))
            .count();

    double ratio = (nightCount * 100.0) / total;
    if (ratio < 70) return ratio - 70;
    return Math.min(100, 50 + ((ratio - 70) * 1.66));
  }

  /** 유형 6: 섬세한 예민형 나오 (환경 루틴형) - 집중도 4점 이상 기록 간 환경 태그 유사성 80% 이상 */
  private double calcType6Score(List<Post> posts) {
    List<Post> highFocus =
        posts.stream().filter(p -> p.getFocus() != null && p.getFocus() >= 4).toList();
    if (highFocus.size() < 2) return -100;

    // 각 Post의 태그 ID 집합 추출
    List<Set<Long>> tagSets =
        highFocus.stream()
            .map(
                p ->
                    p.getTags().stream().map(pt -> pt.getTag().getId()).collect(Collectors.toSet()))
            .toList();

    // 전체 고집중 기록들 간의 평균 Jaccard 유사도 계산
    double totalSimilarity = 0;
    int pairCount = 0;
    for (int i = 0; i < tagSets.size(); i++) {
      for (int j = i + 1; j < tagSets.size(); j++) {
        totalSimilarity += jaccardSimilarity(tagSets.get(i), tagSets.get(j));
        pairCount++;
      }
    }

    double avgSimilarity = pairCount > 0 ? (totalSimilarity / pairCount) * 100 : 0;
    if (avgSimilarity < 80) return avgSimilarity - 80;
    return Math.min(100, 50 + ((avgSimilarity - 80) * 2.5));
  }

  // ========== 집중 환경 분석 ==========

  private FocusEnvironmentResponse analyzeFocusEnvironment(List<Post> posts) {
    // 영역 1: 시간대별 몰입 분석
    String bestPeriod = null;
    Double bestPeriodAvg = null;

    Map<String, List<Integer>> periodFocusMap = new HashMap<>();
    for (Post p : posts) {
      if (p.getStartedAt() == null || p.getFocus() == null) continue;
      String period = resolveTimePeriod(p);
      periodFocusMap.computeIfAbsent(period, k -> new ArrayList<>()).add(p.getFocus());
    }

    // 4-5점 기록이 가장 많은 시간대 찾기
    Map<String, Long> highFocusCountByPeriod = new HashMap<>();
    for (var entry : periodFocusMap.entrySet()) {
      long count = entry.getValue().stream().filter(f -> f >= 4).count();
      highFocusCountByPeriod.put(entry.getKey(), count);
    }

    boolean hasHighFocus = highFocusCountByPeriod.values().stream().anyMatch(c -> c > 0);

    if (hasHighFocus) {
      bestPeriod =
          highFocusCountByPeriod.entrySet().stream()
              .filter(e -> e.getValue() > 0)
              .max(
                  Comparator.<Map.Entry<String, Long>, Long>comparing(Map.Entry::getValue)
                      .thenComparingDouble(
                          e ->
                              periodFocusMap.get(e.getKey()).stream()
                                  .mapToInt(Integer::intValue)
                                  .average()
                                  .orElse(0)))
              .map(Map.Entry::getKey)
              .orElse(null);
    } else {
      // 4-5점 기록이 없으면, 평균 focus가 가장 높은 시간대
      bestPeriod =
          periodFocusMap.entrySet().stream()
              .max(
                  Comparator.comparingDouble(
                      e -> e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0)))
              .map(Map.Entry::getKey)
              .orElse(null);
    }

    if (bestPeriod != null && periodFocusMap.containsKey(bestPeriod)) {
      bestPeriodAvg =
          periodFocusMap.get(bestPeriod).stream().mapToInt(Integer::intValue).average().orElse(0);
      bestPeriodAvg = Math.round(bestPeriodAvg * 10) / 10.0;
    }

    // 영역 2: 태그별 평균 집중도 - 가장 높은 태그
    Long bestTagId = null;
    PlaceTag bestPlaceTag = null;
    Double bestPlaceTagAvgFocus = null;

    Map<Long, PlaceTag> placeTags = new HashMap<>();
    Map<Long, List<Integer>> tagFocusMap = new HashMap<>();
    for (Post p : posts) {
      if (p.getFocus() == null) continue;
      for (PostTag pt : p.getTags()) {
        Tag tag = pt.getTag();
        placeTags.put(tag.getId(), tag.getPlaceTag());
        tagFocusMap.computeIfAbsent(tag.getId(), k -> new ArrayList<>()).add(p.getFocus());
      }
    }

    if (!tagFocusMap.isEmpty()) {
      bestTagId =
          tagFocusMap.entrySet().stream()
              .max(
                  Comparator.<Map.Entry<Long, List<Integer>>, Double>comparing(
                          e ->
                              e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0))
                      .thenComparingInt(e -> e.getValue().size()))
              .map(Map.Entry::getKey)
              .orElse(null);
      bestPlaceTag = bestTagId != null ? placeTags.get(bestTagId) : null;
      if (bestTagId != null) {
        double raw =
            tagFocusMap.get(bestTagId).stream().mapToInt(Integer::intValue).average().orElse(0);
        bestPlaceTagAvgFocus = Math.round(raw * 10) / 10.0;
      }
    }

    // 영역 3: 방해 요소 - 평균 집중도가 낮거나 점수 편차가 큰 태그
    // [개선] 단순 평균 최솟값 → (낮은 평균 + 높은 분산) 복합 점수로 교체
    // 기획서 요구사항: "평균 점수가 가장 낮거나 기록 간 점수 편차가 큰 환경 태그를 분석"
    Long worstTagId = null;
    PlaceTag worstPlaceTag = null;
    Double worstPlaceTagAvgFocus = null;

    if (!tagFocusMap.isEmpty()) {
      worstTagId =
          tagFocusMap.entrySet().stream()
              .min(
                  Comparator.comparingDouble(
                      e -> {
                        List<Integer> focuses = e.getValue();
                        double avg =
                            focuses.stream().mapToInt(Integer::intValue).average().orElse(5.0);
                        // 분산(Variance) = E[(X - μ)²]
                        double variance =
                            focuses.stream()
                                .mapToDouble(f -> Math.pow(f - avg, 2))
                                .average()
                                .orElse(0.0);
                        // 복합 점수: 평균이 낮을수록, 분산이 클수록 값이 작아짐 (낮은 값이 worst)
                        // 가중치: 평균(70%) + 분산 패널티(30%)
                        // 0~5점 척도에서 이론상 최대 분산은 6.25((5-0)^2 / 4)이므로 정규화 후 차감
                        return avg * 0.7 - (variance / 6.25) * 0.3 * 5;
                      }))
              .map(Map.Entry::getKey)
              .orElse(null);

      worstPlaceTag = worstTagId != null ? placeTags.get(worstTagId) : null;
      if (worstTagId != null) {
        double raw =
            tagFocusMap.get(worstTagId).stream().mapToInt(Integer::intValue).average().orElse(0);
        worstPlaceTagAvgFocus = Math.round(raw * 10) / 10.0;
      }
    }

    log.info(
        "[MemberAnalytics] Focus Environment - Best Period: {} (Avg: {}), Best Tag: {} ({}), Worst Tag: {} ({})",
        bestPeriod,
        bestPeriodAvg,
        bestTagId,
        bestPlaceTag,
        worstTagId,
        worstPlaceTag);

    return new FocusEnvironmentResponse(
        bestPeriod,
        bestPeriodAvg,
        bestPlaceTag,
        bestPlaceTagAvgFocus,
        worstPlaceTag,
        worstPlaceTagAvgFocus);
  }

  // ========== 공간별 순위 분석 ==========

  private List<SpaceRankingResponse> analyzeSpaceRanking(List<Post> posts) {
    // PlaceCategory별 Post 그룹화
    Map<PlaceCategoryCode, List<Integer>> categoryFocusMap = new HashMap<>();
    Map<PlaceCategoryCode, Integer> categoryCountMap = new HashMap<>();

    for (Post p : posts) {
      PlaceCategoryCode placeCategory = p.getPlaceCategory();
      if (placeCategory == null) {
        continue;
      }

      categoryCountMap.merge(placeCategory, 1, Integer::sum);

      if (p.getFocus() != null) {
        categoryFocusMap.computeIfAbsent(placeCategory, k -> new ArrayList<>()).add(p.getFocus());
      }
    }

    if (categoryCountMap.isEmpty()) {
      return List.of();
    }

    // 빈도순 정렬, 동점 시 평균 집중도 높은 순
    return categoryCountMap.entrySet().stream()
        .sorted(
            Comparator.<Map.Entry<PlaceCategoryCode, Integer>, Integer>comparing(
                    Map.Entry::getValue)
                .reversed()
                .thenComparing(
                    e -> {
                      List<Integer> focuses = categoryFocusMap.getOrDefault(e.getKey(), List.of());
                      return focuses.isEmpty()
                          ? 0.0
                          : focuses.stream().mapToInt(Integer::intValue).average().orElse(0);
                    },
                    Comparator.reverseOrder()))
        .limit(3)
        .map(
            e -> {
              List<Integer> focuses = categoryFocusMap.getOrDefault(e.getKey(), List.of());
              double avg =
                  focuses.isEmpty()
                      ? 0.0
                      : focuses.stream().mapToInt(Integer::intValue).average().orElse(0);
              avg = Math.round(avg * 10) / 10.0;
              int postCount = e.getValue();
              return new SpaceRankingResponse(e.getKey(), postCount, avg);
            })
        .toList();
  }

  // ========== 유틸리티 메서드 ==========

  private List<Post> getRecent(List<Post> posts, int count) {
    return posts.stream()
        .sorted(
            Comparator.comparing(
                Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
        .limit(count)
        .toList();
  }

  /** 시간대 결정: startedAt~endedAt 구간이 하나의 시간대에 속하면 해당 시간대, 아니면 startedAt 기준 */
  private String resolveTimePeriod(Post post) {
    return classifyTime(post.getStartedAt().toLocalTime());
  }

  /**
   * 시간대 분류 (기획서 기준)
   *
   * <ul>
   *   <li>오전: 07:00 ~ 10:50
   *   <li>오후: 11:00 ~ 19:50
   *   <li>밤: 20:00 ~ 23:00
   *   <li>새벽: 그 외 (00:00~06:59, 23:01~23:59)
   * </ul>
   */
  private String classifyTime(LocalTime time) {
    int totalMinutes = time.getHour() * 60 + time.getMinute();
    // 오전: 07:00(420) ~ 10:50(650)
    if (totalMinutes >= 420 && totalMinutes <= 650) return "오전";
    // 오후: 11:00(660) ~ 19:50(1190)
    if (totalMinutes >= 660 && totalMinutes <= 1190) return "오후";
    // 밤: 20:00(1200) ~ 23:00(1380)
    if (totalMinutes >= 1200 && totalMinutes <= 1380) return "밤";
    // 새벽: 그 외 (00:00~06:59, 23:01~23:59)
    return "새벽";
  }

  private double jaccardSimilarity(Set<Long> a, Set<Long> b) {
    if (a.isEmpty() && b.isEmpty()) return 0.0;
    Set<Long> union = new HashSet<>(a);
    union.addAll(b);
    if (union.isEmpty()) return 0.0;
    Set<Long> intersection = new HashSet<>(a);
    intersection.retainAll(b);
    return (double) intersection.size() / union.size();
  }
}
