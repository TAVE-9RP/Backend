package com.nexerp.domain.member.service;

import com.nexerp.domain.member.model.embeddable.ServicePermissions;
import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import com.nexerp.domain.member.model.request.MemberLoginRequestDto;
import com.nexerp.domain.member.model.request.MemberSignupRequestDto;
import com.nexerp.domain.member.model.response.MemberAuthResponseDto;
import com.nexerp.domain.member.model.response.MemberInfoResponseDto;
import com.nexerp.domain.member.repository.MemberRepository;
import com.nexerp.domain.member.util.EnumValidatorUtil;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.security.details.CustomUserDetails;
import com.nexerp.global.security.details.CustomUserDetailsService;
import com.nexerp.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService customUserDetailsService;

  // 회원가입
  @Transactional
  public Long signUp(MemberSignupRequestDto memberSignupRequestDto) {
    try {
      String encodedPassword = passwordEncoder.encode(memberSignupRequestDto.getPassword());

      MemberDepartment department = EnumValidatorUtil.validateDepartment(
        memberSignupRequestDto.getDepartment());
      MemberPosition position = EnumValidatorUtil.validatePosition(
        memberSignupRequestDto.getPosition());

      Member member = Member.builder()
        .loginId(memberSignupRequestDto.getLoginId())
        .password(encodedPassword)
        .name(memberSignupRequestDto.getName())
        .email(memberSignupRequestDto.getEmail())
        .department(department)
        .position(position)
        .companyId(memberSignupRequestDto.getCompanyId())
        .build();

      if (position == MemberPosition.OWNER) {
        member.setPermissions(ServicePermissions.createForOwner());
      } else {
        member.setPermissions(ServicePermissions.createForEmployee(department));
      }
      Member savedMember = memberRepository.save(member);
      return savedMember.getId();

    } catch (DataIntegrityViolationException e) {
      throw new BaseException(GlobalErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 아이디 또는 이메일입니다.");
    }


  }

  // 로그인
  @Transactional
  public MemberAuthResponseDto login(MemberLoginRequestDto memberLoginRequestDto) {

    // 로그인용 id와 pw를 스프링 시큐리티가 처리할 수 있는 인증용 토큰로 변환
    UsernamePasswordAuthenticationToken authenticationToken =
      new UsernamePasswordAuthenticationToken(memberLoginRequestDto.getLoginId(),
        memberLoginRequestDto.getPassword());

    try {
      // authenticationManager가 id와 pw 검증
      Authentication authentication = authenticationManager.authenticate(authenticationToken);

      // Access Token, Refresh Token 생성
      MemberAuthResponseDto tokenDto = jwtTokenProvider.generateToken(authentication);

      // 로그인된 사용자 정보 가져오기
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

      // 오너는 승인 검증 생략
      if (userDetails.getMember().getPosition() != MemberPosition.OWNER &&
        userDetails.getMember().getRequestStatus() != MemberRequestStatus.APPROVED) {
        throw new BaseException(
          GlobalErrorCode.UNAUTHORIZED,
          "가입 요청이 승인되지 않은 계정입니다."
        );
      }

      // 클라이언트에 반환할 DTO 생성 (RefreshToken 포함, 쿠키 처리는 컨트롤러에서)
      return MemberAuthResponseDto.builder()
        .accessToken(tokenDto.getAccessToken())
        .refreshToken(tokenDto.getRefreshToken()) // 쿠키용으로 반환
        .build();

    } catch (AuthenticationException e) {
      throw new BaseException(
        GlobalErrorCode.BAD_REQUEST,
        "아이디 또는 비밀번호가 일치하지 않습니다."
      );
    }
  }

  // 새로운 AT와 RT를 발급
  @Transactional
  public MemberAuthResponseDto reissueToken(String expiredAccessToken, String refreshToken) {

    // RT 유효성 검사 (위변조 여부)
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new BaseException(GlobalErrorCode.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다.");
    }

    // 만료되지 않은 AT가 들어온 경우 거부
    if (!jwtTokenProvider.isTokenExpired(expiredAccessToken)) {
      throw new BaseException(GlobalErrorCode.STATE_CONFLICT, "Access Token이 아직 유효하여 재발급할 수 없습니다.");
    }

    // 만료된 AT에서 memberId 추출
    String memberId = jwtTokenProvider.getMemberIdFromExpiredToken(expiredAccessToken);

    // Authentication 객체 생성 (DB 조회 포함)
    Authentication authentication = customUserDetailsService.createAuthenticationById(memberId);

    // 새 토큰 발급
    MemberAuthResponseDto newTokens = jwtTokenProvider.generateToken(authentication);

    return newTokens;
  }


  @Transactional(readOnly = true)
  public MemberInfoResponseDto getMemberInfo(Long memberId) {
    Member member = getMemberByMemberId(memberId);
    return MemberInfoResponseDto.form(member);
  }

  @Transactional(readOnly = true)
  public Long getCompanyIdByMemberId(Long memberId) {
    Member member = memberRepository.findById(memberId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "로그인한 회원 정보를 찾을 수 없습니다."));

    return member.getCompanyId();
  }

  @Transactional(readOnly = true)
  public Member getMemberByMemberId(Long memberId) {
    Member member = memberRepository.findById(memberId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND,
        String.format("회원 정보를 찾을 수 없습니다. (ID: %d)", memberId)));
    return member;
  }
}
