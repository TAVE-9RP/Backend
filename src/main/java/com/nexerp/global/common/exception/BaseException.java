package com.nexerp.global.common.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

  private final ErrorCode errorCode;

  public BaseException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public BaseException(ErrorCode errorCode, String message) {
    super(errorCode.getMessage() + " -> " + message);
    this.errorCode = errorCode;
  }
}
