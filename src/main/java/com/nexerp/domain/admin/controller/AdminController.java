package com.nexerp.domain.admin.controller;

import com.nexerp.domain.admin.model.request.JoinStatusUpdateRequest;
import com.nexerp.domain.admin.model.request.PermissionUpdateRequest;
import com.nexerp.domain.admin.model.response.AdminInfoResponse;
import com.nexerp.domain.admin.model.response.JoinStatusResponse;
import com.nexerp.domain.admin.model.response.PermissionResponse;
import com.nexerp.domain.admin.service.AdminService;
import com.nexerp.domain.logistics.service.LogisticsService;
import com.nexerp.domain.member.model.response.MemberInfoResponseDto;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
      오너가 승인 요청된 출하 업무를 승인(IN_PROGRESS) 상태로 변경합니다.
      PENDING 상태에서만 승인 가능
      승인 이후 출하 처리 가능
      """
  )
  public BaseResponse<Void> logisticsApproval(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId) {
    Long ownerId = userDetails.getMemberId();
    logisticsService.approveLogistics(ownerId, logisticsId);

    return BaseResponse.success();
  }

  // 입고 승인 처리
  @PatchMapping("/inventory/{inventoryId}/approve")
  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @Operation(summary = "입고 승인 처리(오너)",
    description = """
      오너가 승인 요청된 입고 업무를 승인(IN_PROGRESS) 상태로 변경합니다.
      PENDING 상태에서만 승인 가능
      승인 이후 입고 처리 가능
      """)
  public BaseResponse<Void> approveInventory(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long inventoryId
  ) {
    Long ownerId = userDetails.getMemberId();
    adminService.approveInventory(ownerId, inventoryId);
    return BaseResponse.success();
  }

  @PatchMapping("/inventory/{inventoryId}/reject")
  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @Operation(summary = "입고 거절 처리(오너)",
    description = """
      오너가 승인 요청된 입고 업무를 거절(REJECT) 상태로 변경합니다.
      PENDING 상태에서만 거절 가능하며, 거절 시 수정 후 재승인 요청이 필요합니다.
      """)
  public BaseResponse<Void> rejectInventory(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long inventoryId
  ) {
    Long ownerId = userDetails.getMemberId();
    adminService.rejectInventory(ownerId, inventoryId);
    return BaseResponse.success();
  }

  @PatchMapping("/logistics/{logisticsId}/reject")
  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @Operation(summary = "출하 거절 처리(오너)",
    description = """
      오너가 승인 요청된 출하 업무를 거절(REJECT) 상태로 변경합니다.
      PENDING 상태에서만 거절 가능하며, 거절 시 수정 후 재승인 요청이 필요합니다.
      """)
  public BaseResponse<Void> rejectLogistics(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId) {
    Long ownerId = userDetails.getMemberId();
    adminService.rejectLogistics(ownerId, logisticsId);

    return BaseResponse.success();
  }

  @GetMapping("/info")
  @Operation(
    summary = "관리자 정보 조회 API",
    description = """
      회사에 소속된 모든 출하 업무 리스트 중 키워드를 통해 조회합니다.
      - **반환 정보:**
      - companyId (회사 id)
      - adminId
      - adminName
      - adminEmail
      """
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "관리자 조회 성공",
      content = @Content(
        mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = MemberInfoResponseDto.class)),
        examples = @ExampleObject(
          name = "성공 예시",
          value = """
            {
                 "timestamp": "2026-01-12T05:33:02.431596100Z",
                 "isSuccess": true,
                 "status": 200,
                 "code": "SUCCESS",
                 "message": "요청에 성공했습니다.",
                 "result": [
                     {
                         "companyId": 1,
                         "adminId": 1,
                         "adminName": "MANAGEMENT",
                         "adminEmail": "test@string"
                     }
                 ]
             }
            """
        )
      )
    )
  })
  public BaseResponse<List<AdminInfoResponse>> getAdminInfo(
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long memberId = userDetails.getMemberId();
    List<AdminInfoResponse> result = adminService.getAdminInfo(memberId);
    return BaseResponse.success(result);
  }

}
