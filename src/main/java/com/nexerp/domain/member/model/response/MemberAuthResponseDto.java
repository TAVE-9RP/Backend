package com.nexerp.domain.member.model.response;

import com.nexerp.domain.member.model.enums.MemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
// 로그인 성공 시 반환 DTO
public class MemberAuthResponseDto {
    // JWT Access Token
    private String accessToken;

    // JWT Refresh Token
    private String refreshToken;

    // 즉시 필요한 정보들은 토큰 디코딩 없이 사용할 수 있도록 '로그인용 ID, 권한' 추가 반환
    // 로그인용 ID
    private String loginId;

    // 권한
    private MemberRole role;
}
