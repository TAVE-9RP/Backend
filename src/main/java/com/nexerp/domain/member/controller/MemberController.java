package com.nexerp.domain.member.controller;

import com.nexerp.domain.member.model.request.MemberLoginRequestDto;
import com.nexerp.domain.member.model.request.MemberSignupRequestDto;
import com.nexerp.domain.member.model.response.MemberAuthResponseDto;
import com.nexerp.domain.member.service.MemberService;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.jwt.JwtTokenProvider;
import com.nexerp.global.security.util.JwtCookieHeaderUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtCookieHeaderUtil jwtCookieHeaderUtil;

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

            // 쿠키 처리
            jwtCookieHeaderUtil.addRefreshTokenCookie(response, authResponse.getRefreshToken());

            // AccessToken 반환
            MemberAuthResponseDto responseDto = MemberAuthResponseDto.builder()
              .accessToken(authResponse.getAccessToken())
              .build();

          return BaseResponse.success(responseDto);
    }

    // AT, RT 재발급
    @PostMapping("/reissue")
    public BaseResponse<MemberAuthResponseDto> reissueToken(
            @RequestHeader("Authorization") String authorizationHeader, // 만료된 AT
            @CookieValue(value = "refresh_token") String refreshToken,  // RT
            HttpServletResponse response // 새 RT를 쿠키로 설정
    ) {
        // Authorization 헤더에서 Bearer 제거
        String expiredAccessToken = authorizationHeader.substring(7);

        // 새 AT와 RT를 포함한 DTO를 반환
        MemberAuthResponseDto newAuthResponse = memberService.reissueToken(expiredAccessToken, refreshToken);

        // 쿠키 처리
        jwtCookieHeaderUtil.addRefreshTokenCookie(response, newAuthResponse.getRefreshToken());


        // AccessToken 반환
        MemberAuthResponseDto responseBody = MemberAuthResponseDto.builder()
                .accessToken(newAuthResponse.getAccessToken())
                .build();

        return BaseResponse.success(responseBody);
    }
}
