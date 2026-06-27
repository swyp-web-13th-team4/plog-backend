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
  INVALID_SIGNUP_TOKEN(HttpStatus.BAD_REQUEST, ErrorCode.E1001, "유효하지 않은 가입 토큰입니다.", LogLevel.WARN),
  INVALID_REFRESH_TOKEN(
      HttpStatus.UNAUTHORIZED, ErrorCode.E1002, "유효하지 않은 리프레시 토큰입니다.", LogLevel.WARN),
  EXPIRED_REFRESH_TOKEN(
      HttpStatus.UNAUTHORIZED, ErrorCode.E1003, "만료된 리프레시 토큰입니다. 다시 로그인해주세요.", LogLevel.WARN),
  INVALID_NICKNAME_FORMAT(
      HttpStatus.BAD_REQUEST,
      ErrorCode.E1004,
      "닉네임은 한글, 영문, 숫자, 언더바(_)만 사용할 수 있습니다.",
      LogLevel.WARN),
  DUPLICATE_NICKNAME(HttpStatus.CONFLICT, ErrorCode.E1005, "이미 사용 중인 닉네임입니다.", LogLevel.WARN),
  INVALID_INTRODUCTION_FORMAT(
      HttpStatus.BAD_REQUEST,
      ErrorCode.E1006,
      "소개글에 연락처, 이메일, SNS 계정 등 개인정보를 포함할 수 없습니다.",
      LogLevel.WARN),
  CONTAINS_BAD_WORD(HttpStatus.BAD_REQUEST, ErrorCode.E1007, "금칙어가 포함되어 있습니다.", LogLevel.WARN),

  // 파일/이미지 관련
  FILE_EMPTY(HttpStatus.BAD_REQUEST, ErrorCode.E1100, "업로드할 파일이 없습니다.", LogLevel.WARN),
  FILE_TYPE_INVALID(HttpStatus.BAD_REQUEST, ErrorCode.E1101, "이미지 파일만 업로드 가능합니다.", LogLevel.WARN),
  FILE_SIZE_EXCEEDED(
      HttpStatus.BAD_REQUEST, ErrorCode.E1102, "파일 크기는 10MB를 초과할 수 없습니다.", LogLevel.WARN),
  FILE_UPLOAD_FAILED(
      HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E1103, "파일 업로드 중 오류가 발생했습니다.", LogLevel.ERROR),
  POST_IMAGE_LIMIT_EXCEEDED(
      HttpStatus.BAD_REQUEST, ErrorCode.E1104, "게시글 이미지는 최대 5개까지 업로드 가능합니다.", LogLevel.WARN),

  // 도메인 NOT_FOUND 세분화
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "해당 회원을 찾을 수 없습니다.", LogLevel.WARN),
  POST_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "해당 게시글을 찾을 수 없습니다.", LogLevel.WARN),
  POST_FORBIDDEN(HttpStatus.FORBIDDEN, ErrorCode.E403, "본인의 게시글만 조회/수정할 수 있습니다.", LogLevel.WARN),
  POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, ErrorCode.E403, "접근 권한이 없는 게시물 입니다.", LogLevel.WARN),
  TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "해당 약관을 찾을 수 없습니다.", LogLevel.WARN),
  REQUIRED_TERMS_NOT_AGREED(
      HttpStatus.BAD_REQUEST, ErrorCode.E400, "필수 약관에 동의해야 합니다.", LogLevel.WARN),
  INVALID_DEFAULT_IMAGE_URL(
      HttpStatus.BAD_REQUEST, ErrorCode.E1105, "등록되지 않은 기본 프로필 이미지 URL입니다.", LogLevel.WARN),

  // 뱃지 관련
  BADGE_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E1200, "해당 뱃지를 찾을 수 없습니다.", LogLevel.WARN),
  BADGE_NOT_OWNED(
      HttpStatus.FORBIDDEN, ErrorCode.E1201, "보유하지 않은 뱃지는 대표 뱃지로 설정할 수 없습니다.", LogLevel.WARN),
  MAIN_BADGE_NOT_SET(HttpStatus.BAD_REQUEST, ErrorCode.E1202, "설정된 대표 뱃지가 없습니다.", LogLevel.WARN),

  // 테그 관련
  TAG_LIMIT_EXCEEDED(
      HttpStatus.BAD_REQUEST, ErrorCode.E1300, "태그는 최대 5개까지 등록 가능합니다.", LogLevel.WARN),
  TAG_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "저장된 태그 값과 일치하지 않습니다", LogLevel.WARN),

  // 포스트 관련
  INVALID_TITLE_LENGTH(
      HttpStatus.BAD_REQUEST, ErrorCode.E1400, "환경 기록 제목 글자 수는 2글자 이상 20글자 이하 입니다.", LogLevel.WARN),
  INVALID_CONTENTS_LENGTH(
      HttpStatus.BAD_REQUEST,
      ErrorCode.E1401,
      "환경 기록 리뷰 글자수는 20글자 이상 300글자 이하 입니다.",
      LogLevel.WARN),

  // 타임피커 공부시간 관련
  INVALID_STUDY_TIME_RANGE(
      HttpStatus.BAD_REQUEST, ErrorCode.E1402, "공부 종료 시각은 시작 시각 이후여야 합니다.", LogLevel.WARN),
  STUDY_TIME_TOO_LONG(
      HttpStatus.BAD_REQUEST, ErrorCode.E1403, "한 번에 기록 가능한 공부 시간은 최대 24시간입니다.", LogLevel.WARN),

  // 플레이스 관련
  PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "해당 장소를 찾을 수 없습니다.", LogLevel.WARN),

  // 지도 관련
  INVALID_VIEWPORT_RANGE(
      HttpStatus.BAD_REQUEST, ErrorCode.E1500, "남서쪽 좌표가 북동쪽보다 클 수 없습니다.", LogLevel.WARN),
  INVALID_PAGING_LIMIT(
      HttpStatus.BAD_REQUEST, ErrorCode.E1501, "한 번에 조회 가능한 최대 개수는 100개입니다.", LogLevel.WARN),

  // 카테고리 관련
  CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "저장된 카테고리 값과 일치하지 않습니다.", LogLevel.WARN),

  // 최신 검색 관련
  RECENT_SEARCH_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "최근 검색을 찾을 수 없습니다.", LogLevel.WARN),
  RECENT_SEARCH_FORBIDDEN(
      HttpStatus.FORBIDDEN, ErrorCode.E403, "본인의 검색 이력만 삭제할 수 있습니다.", LogLevel.WARN),

  // 장소 리뷰 관련
  PLACE_REVIEW_NOT_FOUND(
      HttpStatus.NOT_FOUND, ErrorCode.E404, "저장된 장소 리뷰 값과 일치하지 않습니다.", LogLevel.WARN),
  PLACE_REVIEW_ALREADY_EXISTS(
      HttpStatus.CONFLICT, ErrorCode.E409, "이미 작성된 장소 리뷰가 있습니다.", LogLevel.WARN),
  PLACE_REVIEW_EDIT_PERIOD_EXPIRED(
      HttpStatus.BAD_REQUEST, ErrorCode.E400, "장소 리뷰 수정 가능 기간이 만료되었습니다.", LogLevel.WARN),
  PLACE_REVIEW_IMAGE_LIMIT_EXCEEDED(
      HttpStatus.BAD_REQUEST, ErrorCode.E1106, "장소 리뷰 이미지는 최대 5개까지 업로드 가능합니다.", LogLevel.WARN),

  // 차단 관련
  ALREADY_BLOCKED(HttpStatus.CONFLICT, ErrorCode.E1600, "이미 차단한 유저입니다.", LogLevel.WARN),
  NOT_BLOCKED(HttpStatus.BAD_REQUEST, ErrorCode.E1601, "차단하지 않은 유저입니다.", LogLevel.WARN),
  BLOCKED_USER_ACCESS(
      HttpStatus.FORBIDDEN, ErrorCode.E1602, "차단한 유저의 콘텐츠에는 접근할 수 없습니다.", LogLevel.WARN),
  CANNOT_BLOCK_SELF(HttpStatus.BAD_REQUEST, ErrorCode.E1603, "자기 자신을 차단할 수 없습니다.", LogLevel.WARN),

  // 공지 관련
  NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E1700, "해당 공지를 찾을 수 없습니다", LogLevel.WARN),

  // 1:1 문의 관련
  INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E1700, "해당 문의를 찾을 수 없습니다", LogLevel.WARN);
  // 여기에 추가해주시고 사용하시면 됩니다.

  private final HttpStatus status;
  private final ErrorCode errorCode;
  private final String message;
  private final LogLevel logLevel;
}
