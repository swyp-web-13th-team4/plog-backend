package com.plog.plogbackend.global.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
  private final boolean success;
  private final String message;
  private final T data;
  private final ErrorInfo error;

  private ApiResponse(boolean success, String message, T data, ErrorInfo error) {
    this.success = success;
    this.message = message;
    this.data = data;
    this.error = error;
  }

  /** 성공 (데이터 없음) */
  public static <T> ApiResponse<T> success(String message) {
    return new ApiResponse<>(true, message, null, null);
  }

  /** 성공 (데이터 있음) */
  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, message, data, null);
  }

  /** 실패 */
  public static <T> ApiResponse<T> error(String message, String errorCode) {
    return new ApiResponse<>(false, message, null, new ErrorInfo(errorCode));
  }

  @Getter
  public static class ErrorInfo {
    private final String code;

    public ErrorInfo(String code) {
      this.code = code;
    }
  }
}
