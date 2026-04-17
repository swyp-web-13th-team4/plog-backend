package com.plog.plogbackend.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
  INVALID_ACCESS_PATH(HttpStatus.BAD_REQUEST, ErrorCode.E400, "잘못된 접근입니다", LogLevel.WARN),
  REQUIRED_AUTH(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "인증이 필요합니다.", LogLevel.WARN),
  FAILED_AUTH(HttpStatus.FORBIDDEN, ErrorCode.E403, "인증에 실패했습니다.", LogLevel.WARN),
  NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "해당 데이터를 찾을 수 없습니다.", LogLevel.WARN),
  ALREADY_REGISTERED_MEMBER(HttpStatus.CONFLICT, ErrorCode.E409, "이미 가입된 회원입니다.", LogLevel.WARN),
  SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "서버에서 오류가 발생했습니다.", LogLevel.ERROR),

  INVALID_AUTH_HEADER(HttpStatus.BAD_REQUEST, ErrorCode.E1000, "올바르지 않은 인증 헤더입니다.", LogLevel.WARN),
  INVALID_SIGNUP_TOKEN(HttpStatus.BAD_REQUEST, ErrorCode.E1001, "유효하지 않은 가입 토큰입니다.", LogLevel.WARN);

  // 여기에 추가해주시고 사용하시면 됩니다.

  private final HttpStatus status;
  private final ErrorCode errorCode;
  private final String message;
  private final LogLevel logLevel;
}
