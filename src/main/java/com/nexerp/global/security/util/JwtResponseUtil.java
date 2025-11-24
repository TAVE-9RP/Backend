package com.nexerp.global.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.common.response.BaseResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class JwtResponseUtil {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * BaseResponse 형태로 JSON 응답을 내려줌
   */
  public static void sendErrorResponse(HttpServletResponse response, GlobalErrorCode errorCode) throws IOException {
    response.setStatus(errorCode.getHttpStatus().value());
    response.setContentType("application/json;charset=UTF-8");

    String body = objectMapper.writeValueAsString(BaseResponse.fail(errorCode));
    response.getWriter().write(body);
  }

  /**
   * 커스텀 메시지와 함께 BaseResponse 반환
   */
  public static void sendErrorResponse(HttpServletResponse response, GlobalErrorCode errorCode, String customMessage) throws IOException {
    response.setStatus(errorCode.getHttpStatus().value());
    response.setContentType("application/json;charset=UTF-8");

    String body = objectMapper.writeValueAsString(BaseResponse.fail(errorCode, customMessage));
    response.getWriter().write(body);
  }
}
