package com.nexerp.domain.member.model.response;

import com.nexerp.domain.member.model.enums.MemberDepartment;
import lombok.Getter;

@Getter
public class MemberIdNameResponseDto {

  private final Long memberId;

  private final String name;

  private final MemberDepartment department;

  public MemberIdNameResponseDto(Long memberId, String name, MemberDepartment department) {
    this.memberId = memberId;
    this.name = name;
    this.department = department;
  }
}
