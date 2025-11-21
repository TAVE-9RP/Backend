package com.nexerp.domain.member.model.response;

import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 로그인 성공 시 반환 DTO
public class MemberAuthResponseDto {
    // JWT Access Token
    private String accessToken;

    // JWT Refresh Token
    private String refreshToken;

    // Access Token 만료 시간
    private Long accessTokenExpirationTime;

    // 빠른 초기 화면 분기를 위해서
    // 부서
    private MemberDepartment department;

    // 직급
    private MemberPosition position;
}
