package com.nexerp.domain.kpi.controller;

import com.nexerp.domain.kpi.model.response.KpiDashboardResponse;
import com.nexerp.domain.kpi.service.KpiSnapshotService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.details.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kpi")
@RequiredArgsConstructor
public class KpiController {

  private final KpiSnapshotService kpiSnapshotService;

  @GetMapping("/dashboard")
  public BaseResponse<KpiDashboardResponse> getMyCompanyDashboard (
    @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
    KpiDashboardResponse response = kpiSnapshotService.getMonthlyKpi(userDetails.getMemberId());

    return BaseResponse.success(response);
  }
}
