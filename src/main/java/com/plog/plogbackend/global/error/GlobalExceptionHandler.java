package com.plog.plogbackend.global.error;

import com.plog.plogbackend.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("[Exception] type={} | message={}", e.getClass().getName(), e.getMessage(), e);
    return ResponseEntity.status(ErrorType.SERVER_ERROR.getStatus())
        .body(ApiResponse.error(ErrorType.SERVER_ERROR));
  }

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
    ErrorType type = e.getErrorType();
    log.warn("[AppException] status={} | errorCode={} | message={}",
        type.getStatus().value(), type.getErrorCode(), type.getMessage(), e);
    return ResponseEntity.status(type.getStatus())
        .body(ApiResponse.error(type, e.getErrorData()));
  }

  /** @Valid 검증 실패 (@RequestBody, @RequestPart 등) */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
    e.getBindingResult().getFieldErrors()
        .forEach(fe -> log.warn("[ValidationError] field={} | rejected={} | msg={}",
            fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()));
    return ResponseEntity.badRequest().body(ApiResponse.error(ErrorType.INVALID_ACCESS_PATH));
  }

  /** @Valid 검증 실패 (@ModelAttribute 등) */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
    e.getBindingResult().getFieldErrors()
        .forEach(fe -> log.warn("[BindError] field={} | rejected={} | msg={}",
            fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()));
    return ResponseEntity.badRequest().body(ApiResponse.error(ErrorType.INVALID_ACCESS_PATH));
  }

  /** multipart 필수 파트 누락 */
  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingPart(MissingServletRequestPartException e) {
    log.warn("[MissingPart] partName={} | message={}", e.getRequestPartName(), e.getMessage());
    return ResponseEntity.badRequest().body(ApiResponse.error(ErrorType.INVALID_ACCESS_PATH));
  }
}
