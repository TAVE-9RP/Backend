package com.nexerp.domain.kpi.model.response;

import com.nexerp.domain.kpi.model.entity.KpiSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class KpiDashboardResponse {
  private Long companyId;
  private LocalDate snapshotDate;

  private final Double safetyStockRate;
  private final Double shipmentLeadTimeAvg;
  private final Double shipmentCompletionRate;
  private final Double projectCompletionRate;
  private final Double longTermTaskRate;
  private final Double turnOverRate;

  public static KpiDashboardResponse from (KpiSnapshot s) {
    return KpiDashboardResponse.builder()
      .companyId(s.getCompanyId())
      .snapshotDate(s.getSnapshotDate())
      .safetyStockRate(s.getSafetyStockRate())
      .shipmentLeadTimeAvg(s.getShipmentLeadTimeAvg())
      .shipmentCompletionRate(s.getShippingCompletionRate())
      .projectCompletionRate(s.getProjectCompletionRate())
      .longTermTaskRate(s.getLongTermTaskRate())
      .turnOverRate(s.getTurnOverRate())
      .build();
  }
}
