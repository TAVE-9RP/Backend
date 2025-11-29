package com.nexerp.domain.admin.controller;

import com.nexerp.domain.admin.model.request.JoinStatusUpdateRequest;
import com.nexerp.domain.admin.model.response.JoinStatusResponse;
import com.nexerp.domain.admin.service.AdminService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="인사 관리 API", description = "인사 서비스 직원 조회 및 변경 기능")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  @Operation(summary = "직원 가입 상태 리스트 조회 API", description = "승인 대기 / 승인 / 거절 상태를 포함한 모든 직원의 가입 상태를 조회합니다.")
  @PreAuthorize("hasRole('ROLE_OWNER')")
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
  @PreAuthorize("hasRole('ROLE_OWNER')")
  @PatchMapping("members/status")
  public BaseResponse<List<JoinStatusResponse>> changeMemberRequestStatus(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody JoinStatusUpdateRequest request
    ) {
      Long ownerId = userDetails.getMemberId();

      adminService.changeMemberRequestStatus(ownerId, request);

      return BaseResponse.success();
  }
}
