package com.nexerp.global.handler;

import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.common.response.BaseResponse;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Hidden
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 비즈니스 예외 처리
   */
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException ex) {
    //예외 발생 정보를 로그로 기록 (개발 용)
    log.warn("커스텀 exception: code={}, message={}",
            ex.getErrorCode().getCode(), ex.getMessage(), ex);

    BaseResponse<Void> body;

    // BaseException의 메시지가 ErrorCode의 기본 메시지와 다르면 커스텀 메시지로 간주하여 사용자에게 해당 메시지를 반환
    if (!ex.getMessage().equals(ex.getErrorCode().getMessage())) {
      body = BaseResponse.fail(ex.getErrorCode(), ex.getMessage());
    } else {
      body = BaseResponse.fail(ex.getErrorCode());
    }

    //응답 객체를 생성하고 HTTP 상태 코드를 설정하여 반환
    return ResponseEntity.status(body.getStatus()).body(body);
  }

  /**
   * Validation 예외 처리 (모든 필드 에러 반환)
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<List<Map<String, String>>>> handleValidationException(
          MethodArgumentNotValidException ex) {

    //발생한 모든 필드 에러 정보를 추출하여 클라이언트에게 전달하기 쉬운 List<Map> 형태로 변환
    var errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> Map.of(
                    "field", fe.getField(),                        // 오류가 발생한 필드 이름
                    "value", String.valueOf(fe.getRejectedValue()),    // 거부된(사용자가 입력한) 값
                    "reason", fe.getDefaultMessage()                   // 오류 메시지 (DTO에 정의된 메시지)
            ))
            .toList();

    var body = BaseResponse.fail(GlobalErrorCode.VALIDATION_ERROR, errors);

    return ResponseEntity.status(body.getStatus()).body(body);
  }

  /**
   * 예상치 못한 서버 내부 예외 처리 (최상위 Exception)
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Void>> handleException(Exception ex) {

    // 예외 정보를 로그로 기록
    log.error("Unexpected exception occurred", ex);

    var body = BaseResponse.<Void>fail(GlobalErrorCode.INTERNAL_SERVER_ERROR);

    return ResponseEntity.status(body.getStatus()).body(body);
  }
}