package com.nexerp.global.security.util;

import com.nexerp.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JwtCookieHeaderUtil {

  private final JwtTokenProvider jwtTokenProvider;

  public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {

    long maxAgeSeconds =
      jwtTokenProvider.getRefreshTokenExpirationTime() / 1000;   // 밀리초 (JWT) → 초 (Http 쿠키)

    // Set-Cookie 헤더 생성
    String cookieHeader = String.format(
      "refresh_token=%s; Max-Age=%d; Path=/; Secure; HttpOnly; SameSite=None",
      refreshToken,
      maxAgeSeconds
    );

    // RT를 HTTP-Only Cookie로 저장하도록 강제
    response.addHeader("Set-Cookie", cookieHeader);
  }

  public void clearRefreshTokenCookie(HttpServletResponse response) {
    // Max-Age를 0으로 설정하여 브라우저가 즉시 쿠키를 삭제하도록 함
    String cookieHeader = "refresh_token=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=None";

    response.addHeader("Set-Cookie", cookieHeader);
  }
}

