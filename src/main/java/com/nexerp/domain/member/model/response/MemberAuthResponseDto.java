package com.nexerp.domain.member.model.response;

import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    // 부서
    private MemberDepartment department;

    // 직급
    private MemberPosition position;
}
