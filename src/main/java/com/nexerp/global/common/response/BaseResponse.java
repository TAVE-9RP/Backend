package com.nexerp.global.common.response;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexerp.global.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"timestamp", "isSuccess", "status", "code", "message", "result"})
public class BaseResponse<T> {

  // 요청 시간을 전달 함으로 로그와 비교하여 문제 해결 가능
  @JsonProperty("timestamp")
  private final String timestamp;

  // 직관적인 성공 실패 유무
  @JsonProperty("isSuccess")
  @Getter(AccessLevel.NONE)
  private final boolean isSuccess;

  // HTTP 상태코드 (200, 400, 404, 500 등)
  @JsonProperty("status")
  private final int status;

  // 도메인 에러코드 ex) USER_001 -> 구체적인 원인 파악
  @JsonProperty("code")
  private final String code;

  @JsonProperty("message")
  private final String message;

  @JsonProperty("result")
  private final T result;

  // 모든 필드를 초기화하는 기본 생성자 (다른 생성자에서 호출됨)
  private BaseResponse(String timestamp, boolean isSuccess, int status,
      String code, String message, T result) {
    this.timestamp = timestamp;
    this.isSuccess = isSuccess;
    this.status = status;
    this.code = code;
    this.message = message;
    this.result = result;
  }

  /**
   * ========== 생성자 (Private) ==========
   **/
  //외부에서 직접 호출 불가, 정적 팩토리 메서드를 통해서만 생성

  // [데이터 포함] 요청에 성공한 경우
  private BaseResponse(T result) {
    this(generateTimestamp(), true, HttpStatus.OK.value(), "SUCCESS", "요청에 성공했습니다.",
        result);
  }

  // [데이터 미포함] 요청에 성공한 경우
  private BaseResponse() {
    this(generateTimestamp(), true, HttpStatus.OK.value(), "SUCCESS", "요청에 성공했습니다.", null);
  }

  // [데이터 미포함] 요청에 실패한 경우
  private BaseResponse(ErrorCode errorCode) {
    this(generateTimestamp(), false, errorCode.getHttpStatus().value(), errorCode.getCode(),
        errorCode.getMessage(),
        null);
  }

  // [데이터 포함] 요청에 실패한 경우
  private BaseResponse(ErrorCode errorCode, T result) {
    this(generateTimestamp(), false, errorCode.getHttpStatus().value(), errorCode.getCode(),
        errorCode.getMessage(),
        result);
  }

  // 커스텀 메세지 입력(단순 실패)
  private BaseResponse(ErrorCode errorCode, String customMessage) {
    this(generateTimestamp(), false, errorCode.getHttpStatus().value(), errorCode.getCode(),
        customMessage,
        null);
  }

  // 시간을 조회하는 정적 메서드
  // UTC, 예: 2025-11-12T06:30:00Z
  private static String generateTimestamp() {
    return ISO_INSTANT.format(java.time.Instant.now());
  }


  /**
   * ========== 정적 팩토리 메서드 (Public API) ==========
   */
// 클라이언트 코드에서 사용하는 응답 생성 메서드

// 1. 성공 응답 (데이터 포함)
  public static <T> BaseResponse<T> success(T result) {
    return new BaseResponse<>(result);
  }

  // 2. 성공 응답 (데이터 미포함)
  public static <T> BaseResponse<T> success() {
    return new BaseResponse<>();
  }

  // 3. 실패 응답 (데이터 미포함, 가장 일반적인 비즈니스 오류)
  public static <T> BaseResponse<T> fail(ErrorCode eerrorCode) {
    return new BaseResponse<>(eerrorCode);
  }

  // 4. 실패 응답 (데이터 포함 - 디버깅/유효성 검사 결과 전달 시 유용)
  public static <T> BaseResponse<T> fail(ErrorCode eerrorCode, T result) {
    return new BaseResponse<>(eerrorCode, result);
  }

  // 5. 실패 응답 (커스텀 메시지 - 오류는 특정 상태 코드를 따르지만 메시지만 변경)
  public static <T> BaseResponse<T> fail(ErrorCode eerrorCode, String customMessage) {
    return new BaseResponse<>(eerrorCode, customMessage);
  }

}