package com.nexerp.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 요청 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 토큰 유효성 검사
        // 토큰이 존재하고, Provider가 검증했을 때 유효한 경우
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰이 유효하면 인증 정보(Authentication) 객체 생성
            Authentication authentication = jwtTokenProvider.getAuthentication(token);

            // SecurityContext에 인증 정보를 잠시 저장 (한 HTTP 요청 동안만 유지)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
