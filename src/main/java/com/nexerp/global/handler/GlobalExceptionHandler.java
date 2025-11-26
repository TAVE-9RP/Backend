package com.nexerp.global.handler;

import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
  public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
    //예외 발생 정보를 로그로 기록 (개발 용)
    log.warn("커스텀 eception: code={}, message={}",
      e.getErrorCode().getCode(), e.getMessage(), e);

    BaseResponse<Void> body;

    // BaseException의 메시지가 ErrorCode의 기본 메시지와 다르면 커스텀 메시지로 간주하여 사용자에게 해당 메시지를 반환
    if (!e.getMessage().equals(e.getErrorCode().getMessage())) {
      body = BaseResponse.fail(e.getErrorCode(), e.getMessage());
    } else {
      body = BaseResponse.fail(e.getErrorCode());
    }

    //응답 객체를 생성하고 HTTP 상태 코드를 설정하여 반환
    return ResponseEntity.status(body.getStatus()).body(body);
  }

  /**
   * Validation 예외 처리 (모든 필드 에러 반환)
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<List<Map<String, String>>>> handleValidationException(
    MethodArgumentNotValidException e) {

    //발생한 모든 필드 에러 정보를 추출하여 클라이언트에게 전달하기 쉬운 List<Map> 형태로 변환
    var errors = e.getBindingResult().getFieldErrors().stream()
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
   * 예상치 못한 서버 내부 예외 처리 (최상위 eception)
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Void>> handleException(Exception e,
    HttpServletRequest request, Authentication authentication) {

    // 1) 요청 정보 추출
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String query = request.getQueryString();
    String clientIp = request.getRemoteAddr();

    // 2) 사용자 id 추출
    String username = "미 회원";
    if (authentication != null && authentication.isAuthenticated()) {
      username = authentication.getName(); // Principal의 UserId을 가져옴
    }

    // 3) 예외 타입에 따른 태그/루트 메시지
    String tag = "UNEXPECTED";

    // 데이터베이스 무결성 예외의 경우 처리
    if (e instanceof DataIntegrityViolationException) {
      tag = "DATA_INTEGRITY";
    }

    log.error(
      "[{}] [회원ID:{}] [IP:{}] method:{} uri:{}{} - eType={} message={} \n{}",
      tag,
      username,
      clientIp,
      method,
      uri,
      (query != null ? "?" + query : ""),
      e.getClass().getName(),
      e.getMessage(),
      e
    );

    var body = BaseResponse.<Void>fail(GlobalErrorCode.INTERNAL_SERVER_ERROR);

    return ResponseEntity.status(body.getStatus()).body(body);
  }

}
