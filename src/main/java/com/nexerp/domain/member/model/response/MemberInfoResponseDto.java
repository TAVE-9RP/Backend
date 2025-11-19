package com.nexerp.domain.member.model.response;

import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import com.nexerp.domain.member.model.enums.MemberRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
// 회원 정보 조회 시 반환 DTO
public class MemberInfoResponseDto {

    // PK: 회원ID
    private Long id;

    // 로그인용 ID
    private String loginId;

    // 회원 이름
    private String name;

    // 이메일
    private String email;

    // 신청일
    private LocalDateTime joinRequestDate;

    // 가입일
    private LocalDateTime joinDate;

    // ENUM: 부서
    private MemberDepartment department;

    // ENUM: 직급
    private MemberPosition position;

    // ENUM: 가입 상태
    private MemberRequestStatus requestStatus;

    // Member Entity의 permissions를 프론트에 보낼때는 나눠서 반환.
    private MemberRole logisticsRole;
    private MemberRole inventoryRole;
    private MemberRole managementRole;


    // FK: 회사 ID
    private Long companyId;
}
