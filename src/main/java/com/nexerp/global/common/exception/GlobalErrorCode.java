package com.nexerp.global.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GlobalErrorCode implements ErrorCode {

  // ===== 4xx: Client Errors =====
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400_BAD_REQUEST", "잘못된 요청입니다."),
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON_400_VALIDATION", "요청 값이 유효하지 않습니다."),
  INVALID_JSON(HttpStatus.BAD_REQUEST, "COMMON_400_INVALID_JSON", "요청 본문을 읽을 수 없습니다."),


  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401_UNAUTHORIZED", "인증이 필요합니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403_FORBIDDEN", "권한이 없습니다."),

  NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404_NOT_FOUND", "리소스를 찾을 수 없습니다."),
  NO_HANDLER(HttpStatus.NOT_FOUND, "COMMON_404_NO_HANDLER", "요청하신 URL이 존재하지 않습니다."),

  TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "COMMON_429_TOO_MANY_REQUESTS",
      "요청이 너무 빈번합니다. 잠시 후 다시 시도하세요."),

  // ===== 5xx: Server Errors =====
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_INTERNAL_SERVER_ERROR",
      "서버 내부의 오류가 발생했습니다."),
  BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "COMMON_502_BAD_GATEWAY", "게이트웨이 오류가 발생했습니다."),
  SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "COMMON_503_SERVICE_UNAVAILABLE",
      "서비스를 일시적으로 사용할 수 없습니다.");


  private final HttpStatus httpStatus;
  private final String code; // 도메인_HTTP코드_에러성격
  private final String message; // 기본 메시지


  GlobalErrorCode(HttpStatus httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
  }

}
