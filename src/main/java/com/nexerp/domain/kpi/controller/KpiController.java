package com.nexerp.domain.kpi.controller;

import com.nexerp.domain.kpi.model.response.KpiDashboardResponse;
import com.nexerp.domain.kpi.service.KpiSnapshotService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.config.SwaggerConfig;
import com.nexerp.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kpi")
@RequiredArgsConstructor
@SecurityRequirement(name = SwaggerConfig.AT_SCHEME)
public class KpiController {

  private final KpiSnapshotService kpiSnapshotService;

  @GetMapping("/dashboard")
  @Operation(
    summary = "전월 KPI 분석 결과 조회 API",
    description = """
      모든 KPI의 전월의 말일을 기준으로 최종 분석 결과를 조회합니다.
      
      ex) 2026년 1월에 사용 중이라면, 2025년 12월 31일의 최종 분석 결과를 조회
      
      safetyStockRate: 안전재고 확보율
      shipmentLeadTimeAvg: 출하 리드타임
      shipmentCompletionRate: 출하 완료율
      projectCompletionRate: 프로젝트 처리 완료율
      longTermTaskRate: 업무 장기 처리율
      turnOverRate: 재고 회전율
      
      """
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "모든 KPI 조회 성공",
      content = @Content(
        mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = KpiDashboardResponse.class)),
        examples = @ExampleObject(
          name = "성공 예시",
          value = """
            [
              {
                "companyId": 1,
                "snapshotDate": "2025-12-31",
                "safetyStockRate": 98.2,
                "shipmentLeadTimeAvg": 48.5,
                "shipmentCompletionRate": 99.0,
                "projectCompletionRate": 85.5,
                "longTermTaskRate": 12.0,
                "turnOverRate": 5.4
              }
            ]
            """
        )
      )
    )
  })
  public BaseResponse<KpiDashboardResponse> getMyCompanyDashboard(
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    KpiDashboardResponse response = kpiSnapshotService.getMonthlyKpi(userDetails.getMemberId());

    return BaseResponse.success(response);
  }
}
