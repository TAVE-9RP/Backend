package com.nexerp.global.security.jwt;

import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.security.util.JwtResponseUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
// HTTP 요청당 한 번만 실행되도록 하는 필터
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  /**
   * HTTP 요청 헤더에서 JWT 토큰을 추출 (Bearer 접두사 제거)
   */
  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    // "Bearer "로 시작하고, 실제 토큰 값이 있는 경우
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      // "Bearer " (7글자) 이후의 토큰 문자열 반환
      return bearerToken.substring(7);
    }
    return null;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
    FilterChain filterChain)
    throws ServletException, IOException {

    String path = request.getServletPath();

    if (path.startsWith("/member/signup") ||
      path.startsWith("/member/login") ||
      path.startsWith("/member/reissue") ||
      path.startsWith("/swagger-ui") ||
      path.startsWith("/v3/api-docs")) {
      filterChain.doFilter(request, response);
      return;
    }
    // 요청 헤더에서 토큰 추출
    String token = resolveToken(request);

    try {
      // 토큰이 존재하고, Provider가 검증했을 때 유효한 경우
      if (token != null) {
        if (jwtTokenProvider.validateToken(token)) {
          // 토큰이 유효하면 인증 정보(Authentication) 객체 생성
          Authentication authentication = jwtTokenProvider.getAuthentication(token);

          // SecurityContext에 인증 정보를 잠시 저장 (한 HTTP 요청 동안만 유지)
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }

      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException e) {
      JwtResponseUtil.sendErrorResponse(response, GlobalErrorCode.UNAUTHORIZED, "토큰이 만료되었습니다.");
    } catch (JwtException e) {
      JwtResponseUtil.sendErrorResponse(response, GlobalErrorCode.UNAUTHORIZED, "토큰이 유효하지 않습니다.");
    } catch (AuthenticationServiceException e) {
      JwtResponseUtil.sendErrorResponse(response, GlobalErrorCode.UNAUTHORIZED, e.getMessage());
    } catch (Exception e) {
      JwtResponseUtil.sendErrorResponse(response, GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }


  }
}
