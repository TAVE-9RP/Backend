package com.nexerp.global.security.permission;

import com.nexerp.domain.member.model.embeddable.ServicePermissions;
import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberRole;
import com.nexerp.global.security.details.CustomUserDetails;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

  @Override
  public boolean hasPermission(Authentication auth, Object target, Object permission) {
    CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

    // department 추출 (String -> ENUM 변환 처리)
    MemberDepartment dept;
    if (target instanceof String s) {
      dept = MemberDepartment.valueOf(s);
    } else if (target instanceof MemberDepartment d) {
      dept = d;
    } else {
      return false;
    }
    MemberRole requiredRole = MemberRole.valueOf(permission.toString());

    ServicePermissions perms = user.getMember().getPermissions();

    return switch (dept) {
      case INVENTORY -> check(perms.getInventoryRole(), requiredRole);
      case LOGISTICS -> check(perms.getLogisticsRole(), requiredRole);
      case MANAGEMENT -> check(perms.getManagementRole(), requiredRole);
    };
  }

  private boolean check(MemberRole actual, MemberRole required) {
    if (actual == null) return false;
    if (actual == MemberRole.ALL) return true;
    if (required == MemberRole.READ) return true; // WRITE >= READ
    return actual == required;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
    return false;
  }
}
