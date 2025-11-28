package com.nexerp.global.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GlobalErrorCode implements ErrorCode {

  // ===== 4xx: Client Errors =====
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400_BAD_REQUEST", "클라이언트 요청 오류"),
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON_400_VALIDATION", "요청 데이터 검증 실패"),
  INVALID_JSON(HttpStatus.BAD_REQUEST, "COMMON_400_INVALID_JSON", "요청 본문 형식이 올바르지 않음"),


  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401_UNAUTHORIZED", "인증 필요"),
  FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403_FORBIDDEN", "접근 권한 없음"),

  NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404_NOT_FOUND", "리소스를 찾을 수 없음"),
  NO_HANDLER(HttpStatus.NOT_FOUND, "COMMON_404_NO_HANDLER", "요청 경로(URL) 오류"),

  CONFLICT(HttpStatus.CONFLICT, "COMMON_409_CONFLICT", "일반적인 상태 충돌"),
  DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "COMMON_409_DUPLICATE_RESOURCE", "데이터 중복 오류"),
  STATE_CONFLICT(HttpStatus.CONFLICT, "COMMON_409_STATE_CONFLICT", "리소스 상태 모순 오류"),
  TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "COMMON_429_TOO_MANY_REQUESTS", "요청 횟수 제한 초과"),

  // ===== 5xx: Server Errors =====
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_INTERNAL_SERVER_ERROR",
    "서버 내부 오류");


  private final HttpStatus httpStatus;
  private final String code; // 도메인_HTTP코드_에러성격
  private final String message; // 기본 메시지


  GlobalErrorCode(HttpStatus httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
  }

}
