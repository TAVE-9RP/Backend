package com.nexerp.domain.admin.controller;

import com.nexerp.domain.admin.model.request.JoinStatusUpdateRequest;
import com.nexerp.domain.admin.model.response.JoinStatusResponse;
import com.nexerp.domain.admin.service.AdminService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.details.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

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

  @PreAuthorize("hasRole('ROLE_OWNER')")
  @PatchMapping("members/status")
  public BaseResponse<List<JoinStatusResponse>> changeMemberRequestStatus(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody JoinStatusUpdateRequest request
    ) {
      Long ownerId = userDetails.getMemberId();

      List<JoinStatusResponse> updated = adminService.changeMemberRequestStatus(ownerId, request);

      return BaseResponse.success(updated);
  }
}
