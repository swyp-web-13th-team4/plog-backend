package com.plog.plogbackend.domain.member.repository;

import com.plog.plogbackend.domain.member.entity.BadWord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BadWordRepository extends JpaRepository<BadWord, Long> {

  /** 금칙어 단어 문자열만 전부 조회 (캐싱용) */
  @Query("SELECT b.word FROM BadWord b")
  List<String> findAllWords();
}
