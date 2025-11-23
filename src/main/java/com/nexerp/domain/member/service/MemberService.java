package com.nexerp.domain.member.service;

import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import com.nexerp.domain.member.model.request.MemberLoginRequestDto;
import com.nexerp.domain.member.model.request.MemberSignupRequestDto;
import com.nexerp.domain.member.model.response.MemberAuthResponseDto;
import com.nexerp.domain.member.repository.MemberRepository;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.security.details.CustomUserDetails;
import com.nexerp.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Transactional
    public Long signUp(MemberSignupRequestDto memberSignupRequestDto){
        // 1. 중복 검사: 아이디
        if(memberRepository.findByLoginId(memberSignupRequestDto.getLoginId()).isPresent()){
            throw new BaseException(GlobalErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 아이디입니다.");
        }

        // 2. 중복 검사: 이메일
        if(memberRepository.findByEmail(memberSignupRequestDto.getEmail()).isPresent()){
            throw new BaseException(GlobalErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(memberSignupRequestDto.getPassword());

        Member member = Member.builder()
                .loginId(memberSignupRequestDto.getLoginId())
                .password(encodedPassword)
                .name(memberSignupRequestDto.getName())
                .email(memberSignupRequestDto.getEmail())
                .department(memberSignupRequestDto.getDepartment())
                .position(memberSignupRequestDto.getPosition())
                .companyId(memberSignupRequestDto.getCompanyId())
                .build();

        Member savedMember = memberRepository.save(member);

        return savedMember.getId();

    }

    // 로그인
    @Transactional
    public MemberAuthResponseDto login(MemberLoginRequestDto memberLoginRequestDto) {

        // 로그인용 id와 pw를 스프링 시큐리티가 처리할 수 있는 인증용 토큰로 변환
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(memberLoginRequestDto.getLoginId(), memberLoginRequestDto.getPassword());

        try {
            // authenticationManager가 id와 pw 검증
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // Access Token, Refresh Token 생성
            MemberAuthResponseDto tokenDto = jwtTokenProvider.generateToken(authentication);
            // 로그인된 사용자 정보 가져오기
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 응답용 Dto 반환
            return MemberAuthResponseDto.builder()
                    .accessToken(tokenDto.getAccessToken())
                    .refreshToken(tokenDto.getRefreshToken())
                    .accessTokenExpirationTime(tokenDto.getAccessTokenExpirationTime())
                    .department(userDetails.getMember().getDepartment())
                    .position(userDetails.getMember().getPosition())
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

        // 만료된 AT에서 사용자 Id(PK) 추출
        Claims claims = jwtTokenProvider.parseClaims(expiredAccessToken);
        String memberId = claims.getSubject();

        // 추출된 ID로 DB에서 사용자 정보를 로드 (ID가 유효한지 재확인)
        Member member = memberRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "토큰에 해당하는 회원을 찾을 수 없습니다."));

        // DB에서 가져온 Member 정보로 CustomUserDetails를 생성하여 Authentication 객체를 만듦
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // CustomUserDetails의 권한 정보를 Authentication 객체에 담기
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        // 새 Access Token 및 Refresh Token 생성
        MemberAuthResponseDto newTokens = jwtTokenProvider.generateToken(newAuthentication);

        // 응답 DTO
        return MemberAuthResponseDto.builder()
                .accessToken(newTokens.getAccessToken())
                .refreshToken(newTokens.getRefreshToken())
                .accessTokenExpirationTime(newTokens.getAccessTokenExpirationTime())
                .department(member.getDepartment())
                .position(member.getPosition())
                .build();
    }
}
