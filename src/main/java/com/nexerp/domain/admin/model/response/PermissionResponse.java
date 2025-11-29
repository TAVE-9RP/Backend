package com.nexerp.domain.admin.model.response;

import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionResponse {
  private long memberId;
  private String name;
  private MemberDepartment department;
  private MemberPosition position;
  private MemberRole currentRole;

  public static PermissionResponse from(Member m) {
    MemberRole role = switch (m.getDepartment()) {
      case LOGISTICS -> m.getPermissions().getLogisticsRole();
      case INVENTORY -> m.getPermissions().getInventoryRole();
      case MANAGEMENT -> m.getPermissions().getManagementRole();
    };

    return PermissionResponse.builder()
      .memberId(m.getId())
      .name(m.getName())
      .department(m.getDepartment())
      .position(m.getPosition())
      .currentRole(role)
      .build();
  }
}
