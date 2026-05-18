package com.plog.plogbackend.global.error;

import com.plog.plogbackend.global.response.ApiResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    logException(e);
    return ResponseEntity.status(ErrorType.SERVER_ERROR.getStatus())
        .body(ApiResponse.error(ErrorType.SERVER_ERROR));
  }

  /** SSE 연결 타임아웃 예외 처리 (응답을 쓰지 않음으로써 HttpMessageNotWritableException 방지) */
  @ExceptionHandler(AsyncRequestTimeoutException.class)
  public ResponseEntity<Void> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
    log.info("[AsyncRequestTimeoutException]: SSE connection timed out.");
    return ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE).build();
  }

  /** validation 에러 (400) */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    String errorMessage =
        e.getBindingResult().getFieldErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));

    log.warn("[Validation Error]: {}", errorMessage);

    return ResponseEntity.status(ErrorType.INVALID_ACCESS_PATH.getStatus())
        .body(ApiResponse.error(ErrorType.INVALID_ACCESS_PATH, errorMessage));
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
