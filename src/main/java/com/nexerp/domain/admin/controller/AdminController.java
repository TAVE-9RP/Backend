package com.nexerp.domain.admin.controller;

import com.nexerp.domain.admin.model.request.JoinStatusUpdateRequest;
import com.nexerp.domain.admin.model.request.PermissionUpdateRequest;
import com.nexerp.domain.admin.model.response.JoinStatusResponse;
import com.nexerp.domain.admin.model.response.PermissionResponse;
import com.nexerp.domain.admin.service.AdminService;
import com.nexerp.domain.logistics.service.LogisticsService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인사 관리 API", description = "인사 서비스 직원 조회 및 변경 기능")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;
  private final LogisticsService logisticsService;

  @Operation(summary = "직원 가입 상태 리스트 조회 API", description = "승인 대기 / 승인 / 거절 상태를 포함한 모든 직원의 가입 상태를 조회합니다.")
  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @GetMapping("/members/statuses")
  public BaseResponse<List<JoinStatusResponse>> getMemberJoinStatusList(
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long ownerId = userDetails.getMemberId();
    List<JoinStatusResponse> result =
      adminService.getMemberJoinStatus(ownerId);

    return BaseResponse.success(result);
  }

  @Operation(summary = "직원 가입 상태 변경 API", description = "직원들의 가입 상태를 변경합니다. (리스트로 받기에 여러명 변경 가능)")
  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @PatchMapping("members/status")
  public BaseResponse<List<JoinStatusResponse>> changeMemberRequestStatus(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody JoinStatusUpdateRequest request
  ) {
    Long ownerId = userDetails.getMemberId();

    adminService.changeMemberRequestStatus(ownerId, request);

    return BaseResponse.success();
  }

  @Operation(summary = "직원 권한 상태 리스트 조회 API", description = "모든 직원의 기본 컬럼을 포함한 권한 상태를 조회합니다.")
  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @GetMapping("/members/permissions")
  public BaseResponse<List<PermissionResponse>> getMemberPermissions(
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long ownerId = userDetails.getMemberId();
    List<PermissionResponse> result = adminService.getMemberPermission(ownerId);
    return BaseResponse.success(result);
  }

  // 직원 권한 변경
  @Operation(summary = "직원 권한 상태 변경 API", description = "직원들의 권한 상태를 변경합니다. (리스트로 받기에 여러명 변경 가능)")
  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @PatchMapping("/members/permissions")
  public BaseResponse<Void> updateMemberPermissions(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody PermissionUpdateRequest request
  ) {
    Long ownerId = userDetails.getMemberId();
    adminService.updateMemberPermissions(ownerId, request);
    return BaseResponse.success();
  }

  // 출하 업무 동의
  @PatchMapping("/logistics/{logisticsId}/approve")
  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @Operation(
    summary = "출하 업무 승인 API",
    description = """
      관리자(또는 오너)가 출하 업무를 승인합니다.
      
      - 상태 전환: APPROVAL_PENDING -> IN_PROGRESS
      - 하위 물품 상태: 출하 업무에 포함된 모든 물품의 상태를 IN_PROGRESS로 변경합니다.
      - 제한: 승인 대기(APPROVAL_PENDING) 상태에서만 승인 가능합니다.
      """
  )
  public BaseResponse<Void> logisticsApproval(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId) {
    Long ownerId = userDetails.getMemberId();
    logisticsService.approveLogistics(ownerId, logisticsId);

    return BaseResponse.success();
  }
}
