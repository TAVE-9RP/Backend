package com.nexerp.domain.member.model.response;

import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// 로그인 성공 시 반환 DTO
public class MemberAuthResponseDto {
    // JWT Access Token
    private final String accessToken;

    // JWT Refresh Token
    private final String refreshToken;

}
