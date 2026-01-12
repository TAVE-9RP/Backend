package com.nexerp.domain.member.controller;

import com.nexerp.domain.member.model.request.MemberLoginRequestDto;
import com.nexerp.domain.member.model.request.MemberSignupRequestDto;
import com.nexerp.domain.member.model.response.MemberAuthResponseDto;
import com.nexerp.domain.member.model.response.MemberInfoResponseDto;
import com.nexerp.domain.member.service.MemberService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.details.CustomUserDetails;
import com.nexerp.global.security.jwt.JwtTokenProvider;
import com.nexerp.global.security.util.JwtCookieHeaderUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Tag(name = "회원 관련 API", description = "회원가입, 로그인, 토큰 재발급")
public class MemberController {

  private final MemberService memberService;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtCookieHeaderUtil jwtCookieHeaderUtil;

  @PostMapping("/signup")
  @Operation(
    summary = "회원가입 api",
    description = "모든 필드는 필수 입력해야합니다. 비밀번호는 최소 8자 이상이어야합니다. 이메일은 이메일 형식이어야합니다. ",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = MemberSignupRequestDto.class),
        examples = @ExampleObject(
          name = "회원가입 예시",
          value = """
            {
              "loginId": "user100",
              "password": "pw1234567",
              "name": "유저100이름",
              "email": "user100@gmail.com",
              "department": "LOGISTICS",
              "position": "INTERN",
              "companyId": "1"
            }
            """
        )
      )
    )
  )
  public BaseResponse<Long> signUp(
    @RequestBody @Valid MemberSignupRequestDto memberSignupRequestDto) {

    Long memberId = memberService.signUp(memberSignupRequestDto);

    return BaseResponse.success(memberId);

  }

  // Access Token: 프론트 측에서 메모리에 저장
  // Refresh Token: HTTP Only Cookie 저장 + SameSite=Lax 속성 (GET일때만, 브라우저가 서버로 쿠키를 전송)

  @PostMapping("/login")
  @Operation(
    summary = "로그인 api",
    description = "아이디와 비밀번호는 필수 입력해야합니다.",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = MemberLoginRequestDto.class),
        examples = @ExampleObject(
          name = "로그인 예시",
          value = """
            {
              "loginId": "user100",
              "password": "pw1234567"
            }
            """
        )
      )
    )
  )
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
  @Operation(
    summary = "토큰 재발급 api",
    description = "헤더 Authorization에 만료된 AT를 입력해야합니다."
  )
  public BaseResponse<MemberAuthResponseDto> reissueToken(
    @RequestHeader("Authorization") String authorizationHeader, // 만료된 AT
    @CookieValue(value = "refresh_token") String refreshToken,  // RT
    HttpServletResponse response // 새 RT를 쿠키로 설정
  ) {
    // Authorization 헤더에서 Bearer 제거
    String expiredAccessToken = authorizationHeader.substring(7);

    // 새 AT와 RT를 포함한 DTO를 반환
    MemberAuthResponseDto newAuthResponse = memberService.reissueToken(expiredAccessToken,
      refreshToken);

    // 쿠키 처리
    jwtCookieHeaderUtil.addRefreshTokenCookie(response, newAuthResponse.getRefreshToken());

    // AccessToken 반환
    MemberAuthResponseDto responseBody = MemberAuthResponseDto.builder()
      .accessToken(newAuthResponse.getAccessToken())
      .build();

    return BaseResponse.success(responseBody);
  }

  @GetMapping("/me")
  @Operation(
    summary = "본인 정보 조회 API",
    description = """
      회사에 소속된 모든 출하 업무 리스트 중 키워드를 통해 조회합니다.
      - **반환 정보:**
      - companyId (회사 id)
      - memberId
      - name (회원 이름)
      - email
      - department (소속 부서)
      - position (회사 직급)
      - requestStatus (가입 상태)
      - logisticsRole (출하 권한)
      - inventoryRole (입고 권한)
      - managementRole (관지라 권한(없는 경우 null))
      """
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "정보 조회 성공",
      content = @Content(
        mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = MemberInfoResponseDto.class)),
        examples = @ExampleObject(
          name = "성공 예시",
          value = """
            {
                "timestamp": "2026-01-12T04:26:28.089804600Z",
                "isSuccess": true,
                "status": 200,
                "code": "SUCCESS",
                "message": "요청에 성공했습니다.",
                "result": {
                    "id": 5,
                    "name": "LOGISTICS2",
                    "email": "test@string3",
                    "department": "LOGISTICS",
                    "position": "INTERN",
                    "requestStatus": "APPROVED",
                    "logisticsRole": "WRITE",
                    "inventoryRole": "READ",
                    "managementRole": null,
                    "companyId": 1
                }
            }
            """
        )
      )
    )
  })
  public BaseResponse<MemberInfoResponseDto> getMemberInfo(
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long memberId = userDetails.getMemberId();
    MemberInfoResponseDto responseDto = memberService.getMemberInfo(memberId);
    return BaseResponse.success(responseDto);
  }
}
