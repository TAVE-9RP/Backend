package com.nexerp.domain.kpi.controller;

import com.nexerp.domain.kpi.model.response.KpiDashboardResponse;
import com.nexerp.domain.kpi.model.response.ShipmentLeadTimeChartResponse;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kpi")
@RequiredArgsConstructor
@Tag(name = "KPI 대시보드 관련 API", description = "관리, 재고, 물류 서비스 홈 화면에 들어갈 모든 KPI")
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

  @Operation(
    summary = "전월 KPI 분석 결과 조회 API",
    description = """
      출하 리드타임의 1~12월 결과를 시계열 형식의 연동이 가능하도록 반환합니다.
      """
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "모든 KPI 조회 성공",
      content = @Content(
        mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = ShipmentLeadTimeChartResponse.class)),
        examples = @ExampleObject(
          name = "성공 예시",
          value = """
            [
              {
                "companyId": 1,
                "year": 2025,
                "history": [
                     {
                         "month": "1월",
                         "value" 310.5
                     },
                     {
                         "month": "2월",
                         "value" 325.2
                     },
                     {
                         "month": "3월",
                         "value" 318.0
                     },
                     {
                         "month": "4월",
                         "value" 340.8
                     },
                     {
                         "month": "5월",
                         "value" 355.1
                     },
                     {
                         "month": "6월",
                         "value" 348.4
                     },
                     {
                         "month": "7월",
                         "value" 370.2
                     },
                     {
                         "month": "8월",
                         "value" 385.9
                     },
                     {
                         "month": "10월",
                         "value" 390.1
                     },
                     {
                         "month": "11월",
                         "value" 395.7
                     },
                     {
                         "month": "12월",
                         "value" 397.33
                     },
                 ]
              }
            ]
            """
        )
      )
    )
  })
  @GetMapping("/chart/shipment-lead-time")
  public BaseResponse<ShipmentLeadTimeChartResponse> getLeadTimeChart(@AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getMemberId();
    return BaseResponse.success(kpiSnapshotService.getLeadTimeChartDate(memberId));
  }
}
