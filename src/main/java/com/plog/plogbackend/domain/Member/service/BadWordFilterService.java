package com.plog.plogbackend.domain.Member.service;

import com.plog.plogbackend.domain.Member.repository.BadWordRepository;
import com.plog.plogbackend.global.error.AppException;
import com.plog.plogbackend.global.error.ErrorType;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 금칙어 필터링
 *
 * <p>애플리케이션 시작 시 DB에서 금칙어 목록을 전부 메모리에 캐싱 이후 검사는 문자열 탐색
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BadWordFilterService {

  private final BadWordRepository badWordRepository;

  /** 빌드 배포후 여기에 금칙어 다담음 */
  private Set<String> badWords;

  /**
   * 서버 시작 시 금칙어 목록을 DB에서 읽어 메모리에 캐싱합니다.
   *
   * <p>금칙어가 추가/삭제된 경우 서버를 재시작하거나, 별도의 refresh 엔드포인트를 통해 갱신하세요.
   */
  @PostConstruct
  @Transactional(readOnly = true)
  public void loadBadWords() {
    List<String> words = badWordRepository.findAllWords();
    badWords = words.stream().map(String::toLowerCase).collect(Collectors.toSet());
    log.info("금칙어 목록 로드 완료 - 총 {}개", badWords.size());
  }

  /**
   * 입력 문자열에 금칙어가 포함되어 있으면 {@link AppException}을 던집니다.
   *
   * @param input 검사할 문자열 (null·blank 이면 검사 생략)
   * @throws AppException 금칙어 포함 시 {@link ErrorType#CONTAINS_BAD_WORD}
   */
  public void validate(String input) {
    if (input == null || input.isBlank()) {
      return;
    }

    String lowerInput = input.toLowerCase();
    for (String word : badWords) {
      if (lowerInput.contains(word)) {
        log.warn("금칙어 감지 - 입력값에 '{}' 포함", word);
        throw new AppException(ErrorType.CONTAINS_BAD_WORD);
      }
    }
  }

  /** 런타임에 금칙어 목록을 재로드합니다. */
  @Transactional(readOnly = true)
  public void refresh() {
    loadBadWords();
    log.info("금칙어 목록 갱신 완료");
  }
}
