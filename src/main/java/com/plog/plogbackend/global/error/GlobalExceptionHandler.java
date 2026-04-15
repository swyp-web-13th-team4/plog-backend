package com.plog.plogbackend.global.error;

import com.plog.plogbackend.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<?> handleIllegalArgument(IllegalArgumentException e) {
    log.warn("잘못된 요청: {}", e.getMessage());
    return ApiResponse.error(e.getMessage(), "BAD_REQUEST");
  }
}
