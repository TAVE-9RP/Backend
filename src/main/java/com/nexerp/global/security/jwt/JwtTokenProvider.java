package com.nexerp.global.security.jwt;

import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberRole;
import com.nexerp.domain.member.model.response.MemberAuthResponseDto;
import com.nexerp.domain.member.repository.MemberRepository;
import com.nexerp.global.security.details.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class JwtTokenProvider {     // 토큰 발급

  private final MemberRepository memberRepository;

  private final Key key;      // JWT 서명에 사용
  private final long accessTokenExpirationTime;
  private final long refreshTokenExpirationTime;

  // application-prod.yml의 설정 값을 주입받아 사용
  public JwtTokenProvider(
    MemberRepository memberRepository,
    @Value("${jwt.secret}") String secretKey,
    @Value("${jwt.access-token-expiration-in-milliseconds}") long accessTokenExpirationTime,
    @Value("${jwt.refresh-token-expiration-in-milliseconds}") long refreshTokenExpirationTime
  ) {
    this.memberRepository = memberRepository;
    // Base64 디코딩하여 key 생성
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.accessTokenExpirationTime = accessTokenExpirationTime;
    this.refreshTokenExpirationTime = refreshTokenExpirationTime;
  }

  // Refresh Token 만료 시간을 반환하는 getter
  public long getRefreshTokenExpirationTime() {
    return refreshTokenExpirationTime;
  }

  /**
   * 인증 정보(Authentication)를 기반으로 Access Token과 Refresh Token을 생성
   */
  public MemberAuthResponseDto generateToken(Authentication authentication) {

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Member member = userDetails.getMember();

    long now = (new Date()).getTime();
    Date accessTokenExpiresIn = new Date(now + accessTokenExpirationTime);
    Date refreshTokenExpiresIn = new Date(now + refreshTokenExpirationTime);

    // Access Token 생성 (Subject: 회원 PK, Claim: 권한)
    String accessToken = Jwts.builder()
      .setSubject(String.valueOf(member.getId()))
      .claim("tokenVersion", member.getTokenVersion())
      .claim("department", member.getDepartment().name())
      .setExpiration(accessTokenExpiresIn)
      .signWith(key, SignatureAlgorithm.HS256)
      .compact();

    // Refresh Token 생성 (만료 시간만 포함)
    String refreshToken = Jwts.builder()
      .setSubject(String.valueOf(member.getId()))
      .claim("tokenVersion", member.getTokenVersion())
      .setExpiration(refreshTokenExpiresIn)
      .signWith(key, SignatureAlgorithm.HS256)
      .compact();

    // 응답 DTO 반환 (부서/직급 정보는 MemberService에서 채워짐)
    return MemberAuthResponseDto.builder()
      .accessToken(accessToken)
      .refreshToken(refreshToken)
      .build();
  }

  private String roleOrNone(MemberRole role) {
    return role != null ? role.name() : "NONE";
  }

  /**
   * Access Token 검증하여 Authentication 객체로 반환
   */
  public Authentication getAuthentication(String accessToken) {
    // 클레임 추출
    Claims claims = parseClaims(accessToken);
    Long memberId = Long.valueOf(claims.getSubject());

    Member member = memberRepository.findById(memberId)
      .orElseThrow(() -> new AuthenticationServiceException("토큰에 해당하는 회원이 존재하지 않습니다."));

    Long accessTokenVersion = getTokenVersion(accessToken);

    if (!accessTokenVersion.equals(member.getTokenVersion())) {
      throw new AuthenticationServiceException("토큰이 더 이상 유효하지 않습니다.");
    }

    CustomUserDetails principal = new CustomUserDetails(member);

    return new UsernamePasswordAuthenticationToken(
      principal,
      null,
      List.of()
    );

  }

  /**
   * 토큰 정보를 검증
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      log.info("잘못된 JWT 서명입니다.");
      throw new JwtException("잘못된 JWT 서명입니다."); // 예외를 던짐
    } catch (ExpiredJwtException e) {
      log.info("만료된 JWT 토큰입니다.");
      throw e; // 필터의 catch(ExpiredJwtException)에서 잡을 수 있도록 던짐
    } catch (UnsupportedJwtException e) {
      log.info("지원되지 않는 JWT 토큰입니다.");
      throw new JwtException("지원되지 않는 토큰입니다.");
    } catch (IllegalArgumentException e) {
      log.info("JWT 토큰이 잘못되었습니다.");
      throw new JwtException("JWT 토큰이 잘못되었습니다.");
    }
  }

  // 토큰에서 클레임(Payload) 정보만 추출
  public Claims parseClaims(String accessToken) {
    try {   // 정상 토큰
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
    } catch (ExpiredJwtException e) {   // 만료 토큰
      return e.getClaims();
    }
  }

  /**
   * Access Token이 만료되었는지 확인
   */
  public boolean isTokenExpired(String token) {
    try {
      // 토큰 검증 시 만료되지 않았으면 예외 없이 통과
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return false; // 만료되지 않음
    } catch (ExpiredJwtException e) {
      return true; // 만료됨
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 만료된 AT에서 사용자 Id(PK) 추출
   */
  public String getMemberIdFromExpiredToken(String expiredToken) {
    return getMemberIdFromToken(expiredToken);
  }

  //RT의 소유자(memberId)를 RT 자체에서 추출
  public String getMemberIdFromRefreshToken(String refreshToken) {
    return getMemberIdFromToken(refreshToken);
  }

  private String getMemberIdFromToken(String token) {
    return parseClaims(token).getSubject();
  }

  public Long getTokenVersion(String token) {
    Claims claims = parseClaims(token);
    Object tokenVersion = claims.get("tokenVersion");
    if (tokenVersion == null) {
      throw new AuthenticationServiceException("토큰에 tokenVersion 정보가 없습니다.");
    }
    if (tokenVersion instanceof Number number) {
      return number.longValue();
    }
    return Long.valueOf(tokenVersion.toString());
  }

}
