package com.nexerp.domain.member.model.embeddable;

import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberRole;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
// 회원의 logisticsRole, inventoryRole, managementRole 권한
public class ServicePermissions {
  @Enumerated(EnumType.STRING)
  private MemberRole logisticsRole;

  @Enumerated(EnumType.STRING)
  private MemberRole inventoryRole;

  @Enumerated(EnumType.STRING)
  private MemberRole managementRole;

  // OWNER  기본 권한
  public static ServicePermissions createForOwner() {
    ServicePermissions p = new ServicePermissions();
    p.logisticsRole = MemberRole.ALL;
    p.inventoryRole = MemberRole.ALL;
    p.managementRole = MemberRole.ALL;
    return p;
  }

  // 일반 직원 기본 권한
  public static ServicePermissions createForEmployee(MemberDepartment dept) {
    ServicePermissions p = new ServicePermissions();

    // 관리 서비스는 일반 직원이 어떤 부서여도 null
    p.managementRole = null;

    switch (dept) {
      case LOGISTICS -> {
        p.logisticsRole = MemberRole.WRITE; // 자기 부서
        p.inventoryRole = MemberRole.READ; // 다른 부서
      }
      case INVENTORY -> {
        p.inventoryRole = MemberRole.WRITE; // 자기 부서
        p.logisticsRole = MemberRole.READ; // 다른 부서
      }
    }

    return p;
  }

  // 특정 부서의 권한 업데이트
  public void updateRole(MemberDepartment department, MemberRole newRole) {
    if (newRole == null) return;

    switch (department) {
      case LOGISTICS -> this.logisticsRole = newRole;
      case INVENTORY -> this.inventoryRole = newRole;
      case MANAGEMENT -> this.managementRole = newRole;
      default -> throw new IllegalArgumentException("해당 부서는 권한 업데이트가 불가능합니다.");
    }
  }

  // 사용자가 접근한 부서에 쓰기 권한이 있는지 체크
  // Ex) '재고' 부서 직원이 '물류' 업무에 접근하면, canWrite 메서드를 통해 logisticsRole(READ) 추출 -> isWriteOrAll 메서드에서 WRITE, ALL 권한이 아니므로 쓰기 권한 없다고 판단하는 플로우
  public boolean canWrite(MemberDepartment type) {
    if (type == null) {
      return false;
    }

    return switch (type) {
      case INVENTORY -> isWriteOrAll(this.inventoryRole);
      case LOGISTICS -> isWriteOrAll(this.logisticsRole);
      case MANAGEMENT -> isWriteOrAll(this.managementRole); // 관리 권한 추가
    };
  }

  // WRITE 또는 ALL 권한인지 체크
  private boolean isWriteOrAll(MemberRole role) {
    if (role == null) {
      return false;
    }
    return role == MemberRole.WRITE || role == MemberRole.ALL;
  }

}
