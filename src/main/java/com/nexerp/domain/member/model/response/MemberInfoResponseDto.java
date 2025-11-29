package com.nexerp.domain.member.model.response;

import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import com.nexerp.domain.member.model.enums.MemberRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
// 회원 정보 조회 시 반환 DTO
public class MemberInfoResponseDto {

    // PK: 회원ID
    private final Long id;

    // 로그인용 ID
    private final String loginId;

    // 회원 이름
    private final String name;

    // 이메일
    private final String email;

    // 신청일
    private final LocalDateTime joinRequestDate;

    // 가입일
    private final LocalDateTime joinDate;

    // ENUM: 부서
    private final MemberDepartment department;

    // ENUM: 직급
    private final MemberPosition position;

    // ENUM: 가입 상태
    private final MemberRequestStatus requestStatus;

    // Member Entity의 permissions를 프론트에 보낼때는 나눠서 반환.
    private final MemberRole logisticsRole;
    private final MemberRole inventoryRole;
    private final MemberRole managementRole;


    // FK: 회사 ID
    private final Long companyId;
}
