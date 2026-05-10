package com.plog.plogbackend.global.init;

import jakarta.annotation.PostConstruct;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

  private final JdbcTemplate jdbcTemplate;

  @PostConstruct
  @Transactional
  public void initData() {
    try {
      ClassPathResource resource = new ClassPathResource("init.txt");
      if (!resource.exists()) {
        log.warn("init.txt 파일이 존재하지 않습니다.");
        return;
      }

      String sql;
      try (Reader reader =
          new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
        sql = FileCopyUtils.copyToString(reader);
      }

      List<String> statements = splitSqlScript(sql);

      Set<String> skippedTables = new HashSet<>();
      Set<String> insertedTables = new HashSet<>();

      for (String statement : statements) {
        String trimmed = statement.trim();
        if (trimmed.isEmpty()) continue;

        String tableName = extractTableName(trimmed);
        if (tableName != null) {
          if (skippedTables.contains(tableName)) {
            continue;
          }
          if (!insertedTables.contains(tableName)) {
            Integer count =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
            if (count != null && count > 0) {
              log.info("{} 테이블에 이미 데이터가 존재하여 삽입을 건너뜁니다.", tableName);
              skippedTables.add(tableName);
              continue;
            } else {
              insertedTables.add(tableName);
            }
          }
          jdbcTemplate.execute(trimmed);
        } else {
          jdbcTemplate.execute(trimmed);
        }
      }
      log.info("초기 데이터 삽입 절차가 완료되었습니다.");
    } catch (Exception e) {
      log.error("초기 데이터 삽입 중 오류 발생", e);
    }
  }

  private List<String> splitSqlScript(String script) {
    List<String> statements = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;
    boolean inComment = false;
    char[] chars = script.toCharArray();

    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];

      if (inComment) {
        if (c == '\n') {
          inComment = false;
        }
        continue;
      }

      if (c == '-'
          && i + 1 < chars.length
          && chars[i + 1] == '-'
          && !inSingleQuote
          && !inDoubleQuote) {
        inComment = true;
        i++; // skip second '-'
        continue;
      }

      if (c == '\'' && !inDoubleQuote) {
        if (i > 0 && chars[i - 1] == '\\') {
          // Escaped quote
        } else {
          inSingleQuote = !inSingleQuote;
        }
      } else if (c == '"' && !inSingleQuote) {
        if (i > 0 && chars[i - 1] == '\\') {
          // Escaped quote
        } else {
          inDoubleQuote = !inDoubleQuote;
        }
      }

      if (c == ';' && !inSingleQuote && !inDoubleQuote) {
        statements.add(sb.toString().trim());
        sb.setLength(0);
      } else {
        sb.append(c);
      }
    }
    if (sb.length() > 0 && !sb.toString().trim().isEmpty()) {
      statements.add(sb.toString().trim());
    }
    return statements;
  }

  private String extractTableName(String sql) {
    String normalized = sql.replaceAll("\\s+", " ");
    String upperSql = normalized.toUpperCase();
    if (upperSql.startsWith("INSERT INTO ")) {
      int start = "INSERT INTO ".length();
      int end = upperSql.indexOf(" ", start);
      if (end == -1) end = upperSql.indexOf("(", start);
      if (end != -1) {
        return normalized.substring(start, end).trim();
      }
    } else if (upperSql.startsWith("INSERT IGNORE INTO ")) {
      int start = "INSERT IGNORE INTO ".length();
      int end = upperSql.indexOf(" ", start);
      if (end == -1) end = upperSql.indexOf("(", start);
      if (end != -1) {
        return normalized.substring(start, end).trim();
      }
    }
    return null;
  }
}
