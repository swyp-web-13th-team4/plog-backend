package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.dto.response.FocusEnvironmentResponse;
import com.plog.plogbackend.domain.Member.dto.response.MemberAnalyticsResponse;
import com.plog.plogbackend.domain.Member.dto.response.SpaceRankingResponse;
import com.plog.plogbackend.domain.Member.dto.response.WorkTypeCardResponse;
import com.plog.plogbackend.domain.Member.entity.WorkTypeCard;
import com.plog.plogbackend.domain.Member.repository.MemberRepository;
import com.plog.plogbackend.domain.Member.repository.WorkTypeCardRepository;
import com.plog.plogbackend.domain.post.entity.PlaceCategory;
import com.plog.plogbackend.domain.post.entity.Post;
import com.plog.plogbackend.domain.post.entity.PostCategory;
import com.plog.plogbackend.domain.post.entity.PostTag;
import com.plog.plogbackend.domain.tag.Tag;
import com.plog.plogbackend.domain.tag.enums.PlaceTag;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 분석 정보를 계산하는 서비스.
 *
 * <p>MyPageService에서 위임받아 5가지 분석 결과를 생성합니다.
 */
@Service
@RequiredArgsConstructor
public class MemberAnalyticsService {

  private final MemberRepository memberRepository;
  private final WorkTypeCardRepository workTypeCardRepository;

  private static final int WORK_TYPE_MIN_POSTS = 5;
  private static final int FOCUS_ENV_MIN_POSTS = 15;

  /**
   * 회원의 전체 분석 정보를 조회합니다.
   *
   * @param memberKey 회원 UUID
   * @return 5가지 분석 결과
   */
  @Transactional(readOnly = true)
  public MemberAnalyticsResponse getAnalytics(UUID memberKey) {
    List<Post> posts = memberRepository.findMyPostsForAnalytics(memberKey);
    int totalCount = posts.size();

    int totalStudyTime = posts.stream()
        .filter(p -> p.getStudyTime() != null)
        .mapToInt(Post::getStudyTime)
        .sum();

    WorkTypeCardResponse workTypeCard =
        totalCount >= WORK_TYPE_MIN_POSTS ? analyzeWorkType(posts) : null;

    FocusEnvironmentResponse focusEnv =
        totalCount >= FOCUS_ENV_MIN_POSTS ? analyzeFocusEnvironment(posts) : null;

    List<SpaceRankingResponse> spaceRankings =
        totalCount >= FOCUS_ENV_MIN_POSTS ? analyzeSpaceRanking(posts) : null;

    return new MemberAnalyticsResponse(
        totalCount, totalStudyTime, workTypeCard, focusEnv, spaceRankings);
  }

  // ========== 작업 유형 카드 분석 ==========

  private WorkTypeCardResponse analyzeWorkType(List<Post> posts) {
    Map<Long, Double> scores = new HashMap<>();

    scores.put(1L, calcType1Score(posts));
    scores.put(2L, calcType2Score(posts));
    scores.put(3L, calcType3Score(posts));
    scores.put(4L, calcType4Score(posts));
    scores.put(5L, calcType5Score(posts));
    scores.put(6L, calcType6Score(posts));

    // 조건 미충족(음수 등)인 유형 제거 후 최고 점수 선택
    Long bestTypeId = scores.entrySet().stream()
        .filter(e -> e.getValue() > 0)
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);

    if (bestTypeId == null) {
      return null;
    }

    return workTypeCardRepository.findById(bestTypeId)
        .map(WorkTypeCardResponse::from)
        .orElse(null);
  }

  /** 유형 1: 성실 루틴형 치치 (시간의 규칙성) - 최근 5회 시작 시간 오차 ±30분 이내 */
  private double calcType1Score(List<Post> posts) {
    List<Post> recent5 = getRecent(posts, 5);
    if (recent5.size() < 5) return -1;

    List<LocalTime> times = recent5.stream()
        .filter(p -> p.getStartedAt() != null)
        .map(p -> p.getStartedAt().toLocalTime())
        .toList();
    if (times.size() < 5) return -1;

    // 평균 시작 시간 (분 단위)
    double avgMinutes = times.stream()
        .mapToLong(t -> t.getHour() * 60L + t.getMinute())
        .average().orElse(0);

    // 각 시간과 평균의 차이(분)의 평균
    double avgDeviation = times.stream()
        .mapToDouble(t -> Math.abs((t.getHour() * 60 + t.getMinute()) - avgMinutes))
        .average().orElse(31);

    if (avgDeviation > 30) return -1; // 조건 미충족
    return Math.max(0, 100 - (avgDeviation * 1.66));
  }

  /** 유형 2: 부지런한 아침형 로기 (오전 집중형) - 오전(06:00~10:50) 시작 비중 60% 이상 */
  private double calcType2Score(List<Post> posts) {
    long total = posts.stream().filter(p -> p.getStartedAt() != null).count();
    if (total == 0) return -1;

    long morningCount = posts.stream()
        .filter(p -> p.getStartedAt() != null)
        .filter(p -> {
          LocalTime t = p.getStartedAt().toLocalTime();
          return !t.isBefore(LocalTime.of(6, 0)) && !t.isAfter(LocalTime.of(10, 50));
        })
        .count();

    double ratio = (morningCount * 100.0) / total;
    if (ratio < 60) return -1;
    return Math.min(100, 50 + ((ratio - 60) * 1.25));
  }

  /** 유형 3: 자유로운 탐험형 하루 (장소 유목민형) - 최근 5회 중 PlaceCategory 3곳 이상 */
  private double calcType3Score(List<Post> posts) {
    List<Post> recent5 = getRecent(posts, 5);
    if (recent5.size() < 5) return -1;

    Set<Long> placeCategoryIds = recent5.stream()
        .flatMap(p -> p.getCategories().stream())
        .map(PostCategory::getPlaceCategory)
        .filter(Objects::nonNull)
        .map(PlaceCategory::getId)
        .collect(Collectors.toSet());

    int count = placeCategoryIds.size();
    if (count < 3) return -1;
    return Math.min(100, 50 + ((count - 3) * 25.0));
  }

  /** 유형 4: 빠른 스퍼트형 토리 (단기 몰입형) - 평균 작업 120분 미만 & 2시간 미만 빈도 60% 이상 */
  private double calcType4Score(List<Post> posts) {
    List<Post> withTime = posts.stream()
        .filter(p -> p.getStudyTime() != null)
        .toList();
    if (withTime.isEmpty()) return -1;

    double avg = withTime.stream().mapToInt(Post::getStudyTime).average().orElse(120);
    if (avg >= 120) return -1;

    long shortCount = withTime.stream().filter(p -> p.getStudyTime() < 120).count();
    double shortRatio = (shortCount * 100.0) / withTime.size();
    if (shortRatio < 60) return -1;

    return Math.min(100, 100 - ((avg - 60) * 0.83));
  }

  /** 유형 5: 고요한 새벽형 포포 (올빼미형) - 밤(20:00~23:00) 시작 비중 70% 이상 */
  private double calcType5Score(List<Post> posts) {
    long total = posts.stream().filter(p -> p.getStartedAt() != null).count();
    if (total == 0) return -1;

    long nightCount = posts.stream()
        .filter(p -> p.getStartedAt() != null)
        .filter(p -> {
          LocalTime t = p.getStartedAt().toLocalTime();
          return !t.isBefore(LocalTime.of(20, 0)) && !t.isAfter(LocalTime.of(23, 0));
        })
        .count();

    double ratio = (nightCount * 100.0) / total;
    if (ratio < 70) return -1;
    return Math.min(100, 50 + ((ratio - 70) * 1.66));
  }

  /** 유형 6: 섬세한 예민형 나오 (환경 루틴형) - 집중도 4점 이상 기록 간 환경 태그 유사성 80% 이상 */
  private double calcType6Score(List<Post> posts) {
    List<Post> highFocus = posts.stream()
        .filter(p -> p.getFocus() != null && p.getFocus() >= 4)
        .toList();
    if (highFocus.size() < 2) return -1;

    // 각 Post의 태그 ID 집합 추출
    List<Set<Long>> tagSets = highFocus.stream()
        .map(p -> p.getTags().stream()
            .map(pt -> pt.getTag().getId())
            .collect(Collectors.toSet()))
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
    if (avgSimilarity < 80) return -1;
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
      bestPeriod = highFocusCountByPeriod.entrySet().stream()
          .filter(e -> e.getValue() > 0)
          .max(Map.Entry.comparingByValue())
          .map(Map.Entry::getKey)
          .orElse(null);
    } else {
      // 4-5점 기록이 없으면, 평균 focus가 가장 높은 시간대
      bestPeriod = periodFocusMap.entrySet().stream()
          .max(Comparator.comparingDouble(
              e -> e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0)))
          .map(Map.Entry::getKey)
          .orElse(null);
    }

    if (bestPeriod != null && periodFocusMap.containsKey(bestPeriod)) {
      bestPeriodAvg = periodFocusMap.get(bestPeriod).stream()
          .mapToInt(Integer::intValue).average().orElse(0);
      bestPeriodAvg = Math.round(bestPeriodAvg * 10) / 10.0;
    }

    // 영역 2: 태그별 평균 집중도 - 가장 높은 태그
    Long bestTagId = null;
    PlaceTag bestPlaceTag = null;

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
      bestTagId = tagFocusMap.entrySet().stream()
          .max(Comparator.<Map.Entry<Long, List<Integer>>, Double>comparing(
                  e -> e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0))
              .thenComparingInt(e -> e.getValue().size()))
          .map(Map.Entry::getKey)
          .orElse(null);
      bestPlaceTag = bestTagId != null ? placeTags.get(bestTagId) : null;
    }

    // 영역 3: 방해 요소 - 표준편차가 가장 큰 태그
    Long worstTagId = null;
    PlaceTag worstPlaceTag = null;

    if (!tagFocusMap.isEmpty()) {
      worstTagId = tagFocusMap.entrySet().stream()
          .filter(e -> e.getValue().size() >= 2)
          .max(Comparator.comparingDouble(e -> calcStdDev(e.getValue())))
          .map(Map.Entry::getKey)
          .orElse(null);

      // 편차 기준 결과가 없으면(모두 1개 기록) 가장 낮은 평균의 태그 선택
      if (worstTagId == null) {
        worstTagId = tagFocusMap.entrySet().stream()
            .min(Comparator.comparingDouble(
                e -> e.getValue().stream().mapToInt(Integer::intValue).average().orElse(5)))
            .map(Map.Entry::getKey)
            .orElse(null);
      }

      worstPlaceTag = worstTagId != null ? placeTags.get(worstTagId) : null;
    }

    return new FocusEnvironmentResponse(
        bestPeriod, bestPeriodAvg, bestTagId, bestPlaceTag, worstTagId, worstPlaceTag);
  }

  // ========== 공간별 순위 분석 ==========

  private List<SpaceRankingResponse> analyzeSpaceRanking(List<Post> posts) {
    // PlaceCategory별 Post 그룹화
    Map<Long, String> categoryNames = new HashMap<>();
    Map<Long, List<Integer>> categoryFocusMap = new HashMap<>();
    Map<Long, Integer> categoryCountMap = new HashMap<>();

    for (Post p : posts) {
      for (PostCategory pc : p.getCategories()) {
        PlaceCategory placeCategory = pc.getPlaceCategory();
        if (placeCategory == null) continue;

        Long catId = placeCategory.getId();
        categoryNames.put(catId, placeCategory.getName());
        categoryCountMap.merge(catId, 1, Integer::sum);

        if (p.getFocus() != null) {
          categoryFocusMap.computeIfAbsent(catId, k -> new ArrayList<>()).add(p.getFocus());
        }
      }
    }

    if (categoryCountMap.isEmpty()) {
      return List.of();
    }

    // 빈도순 정렬, 동점 시 평균 집중도 높은 순
    return categoryCountMap.entrySet().stream()
        .sorted(Comparator.<Map.Entry<Long, Integer>, Integer>comparing(Map.Entry::getValue)
            .reversed()
            .thenComparing(e -> {
              List<Integer> focuses = categoryFocusMap.getOrDefault(e.getKey(), List.of());
              return focuses.isEmpty() ? 0.0
                  : focuses.stream().mapToInt(Integer::intValue).average().orElse(0);
            }, Comparator.reverseOrder()))
        .limit(3)
        .map(e -> {
          List<Integer> focuses = categoryFocusMap.getOrDefault(e.getKey(), List.of());
          double avg = focuses.isEmpty() ? 0.0
              : focuses.stream().mapToInt(Integer::intValue).average().orElse(0);
          avg = Math.round(avg * 10) / 10.0;
          return new SpaceRankingResponse(categoryNames.get(e.getKey()), avg);
        })
        .toList();
  }

  // ========== 유틸리티 메서드 ==========

  private List<Post> getRecent(List<Post> posts, int count) {
    return posts.stream()
        .sorted(Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
        .limit(count)
        .toList();
  }

  /** 시간대 결정: startedAt~endedAt 구간이 하나의 시간대에 속하면 해당 시간대, 아니면 startedAt 기준 */
  private String resolveTimePeriod(Post post) {
    return classifyTime(post.getStartedAt().toLocalTime());
  }

  private String classifyTime(LocalTime time) {
    int hour = time.getHour();
    if (hour >= 7 && (hour < 11 || (hour == 10 && time.getMinute() <= 50))) return "오전";
    if (hour >= 11 && (hour < 20 || (hour == 19 && time.getMinute() <= 50))) return "오후";
    if (hour >= 20 && hour <= 23) return "밤";
    return "새벽"; // 0~6
  }

  private double jaccardSimilarity(Set<Long> a, Set<Long> b) {
    if (a.isEmpty() && b.isEmpty()) return 1.0;
    Set<Long> union = new HashSet<>(a);
    union.addAll(b);
    if (union.isEmpty()) return 1.0;
    Set<Long> intersection = new HashSet<>(a);
    intersection.retainAll(b);
    return (double) intersection.size() / union.size();
  }

  private double calcStdDev(List<Integer> values) {
    double avg = values.stream().mapToInt(Integer::intValue).average().orElse(0);
    double variance = values.stream()
        .mapToDouble(v -> Math.pow(v - avg, 2))
        .average().orElse(0);
    return Math.sqrt(variance);
  }
}
