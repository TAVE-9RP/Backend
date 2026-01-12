package com.nexerp.domain.member.model.response;

import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import com.nexerp.domain.member.model.enums.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// 회원 정보 조회 시 반환 DTO
public class MemberInfoResponseDto {

  // FK: 회사 ID
  private final Long companyId;

  // PK: 회원ID
  private final Long memberId;

  // 회원 이름
  private final String name;

  // 이메일
  private final String email;

  // ENUM: 부서
  private final MemberDepartment department;

  // ENUM: 직급
  private final MemberPosition position;

  // ENUM: 가입 상태
  private final MemberRequestStatus requestStatus;

  private final MemberRole logisticsRole;
  private final MemberRole inventoryRole;
  private final MemberRole managementRole;

  public static MemberInfoResponseDto form(Member member) {
    return MemberInfoResponseDto.builder()
      .companyId(member.getCompanyId())
      .memberId(member.getId())
      .name(member.getName())
      .email(member.getEmail())
      .department(member.getDepartment())
      .position(member.getPosition())
      .requestStatus(member.getRequestStatus())
      .logisticsRole(member.getPermissions().getLogisticsRole())
      .inventoryRole(member.getPermissions().getInventoryRole())
      .managementRole(member.getPermissions().getManagementRole())
      .build();
  }
}
