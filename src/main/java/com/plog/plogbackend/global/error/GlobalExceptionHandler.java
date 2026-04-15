package com.plog.plogbackend.global.error;

import com.plog.plogbackend.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    logException(e);
    return ResponseEntity.status(ErrorType.SERVER_ERROR.getStatus())
        .body(ApiResponse.error(ErrorType.SERVER_ERROR));
  }

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
    logAppException(e);
    return ResponseEntity.status(e.getErrorType().getStatus())
        .body(ApiResponse.error(e.getErrorType(), e.getErrorData()));
  }

  private void logAppException(AppException e) {
    StackTraceElement st = e.getStackTrace()[0];
    ErrorType type = e.getErrorType();
    String msg =
        "[AppException]: class="
            + st.getClassName()
            + " | method="
            + st.getMethodName()
            + " | line="
            + st.getLineNumber()
            + " | status="
            + type.getStatus().value()
            + " | errorCode="
            + type.getErrorCode()
            + " | message="
            + type.getMessage();

    switch (type.getLogLevel()) {
      case ERROR -> log.error(msg, e);
      case WARN -> log.warn(msg, e);
      default -> log.info(msg, e);
    }
  }

  private void logException(Exception e) {
    StackTraceElement st = e.getStackTrace()[0];
    log.error(
        "[Exception]: class="
            + st.getClassName()
            + " | method="
            + st.getMethodName()
            + " | line="
            + st.getLineNumber()
            + " | message="
            + e.getMessage());
  }
}
