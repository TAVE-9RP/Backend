package com.nexerp.domain.project.model.response;

import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberDepartment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignListResponse {
  private final Long memberId;
  private final String name;
  private final MemberDepartment department;

  public static AssignListResponse from(Member m) {
    return AssignListResponse.builder()
      .memberId(m.getId())
      .name(m.getName())
      .department(m.getDepartment())
      .build();
  }
}
