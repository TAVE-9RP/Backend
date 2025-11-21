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
}
