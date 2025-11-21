package com.nexerp.domain.member.controller;

import com.nexerp.domain.member.model.request.MemberLoginRequestDto;
import com.nexerp.domain.member.model.request.MemberSignupRequestDto;
import com.nexerp.domain.member.model.response.MemberAuthResponseDto;
import com.nexerp.domain.member.service.MemberService;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public BaseResponse<Long> signUp(@RequestBody @Valid MemberSignupRequestDto memberSignupRequestDto){

        Long memberId = memberService.signUp(memberSignupRequestDto);

        return BaseResponse.success(memberId);

    }

    // Access Token: 프론트 측에서 메모리에 저장
    // Refresh Token: HTTP Only Cookie 저장 + SameSite=Lax 속성 (GET일때만, 브라우저가 서버로 쿠키를 전송)

    @PostMapping("/login")
    public BaseResponse<MemberAuthResponseDto> login(
            @RequestBody @Valid MemberLoginRequestDto memberLoginRequestDto,
            HttpServletResponse response) {     // RT를 HTTP Only Cookie에 저장하기 위해, HttpServletResponse 필요

        MemberAuthResponseDto authResponse = memberService.login(memberLoginRequestDto);

        String refreshToken = authResponse.getRefreshToken();
        long maxAgeSeconds = jwtTokenProvider.getRefreshTokenExpirationTime() / 1000;   // 밀리초 (JWT) → 초 (Http 쿠키)

        // Set-Cookie 헤더 생성
        String cookieHeader = String.format(
                "%s=%s; Max-Age=%d; Path=/; Secure; HttpOnly; SameSite=Lax",
                "refresh_token",
                refreshToken,
                maxAgeSeconds
        );

        // RT를 HTTP-Only Cookie로 저장하도록 강제
        response.addHeader("Set-Cookie", cookieHeader);

        // RT를 제외한 정보 반환
        MemberAuthResponseDto memberAuthResponseDto = MemberAuthResponseDto.builder()
                .accessToken(authResponse.getAccessToken())
                .accessTokenExpirationTime(authResponse.getAccessTokenExpirationTime())
                .department(authResponse.getDepartment())
                .position(authResponse.getPosition())
                .build();

        return BaseResponse.success(memberAuthResponseDto);
    }
}
