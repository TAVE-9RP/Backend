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
  private final Double predShipmentLeadTime;
  private final Double predTurnOverRate;
  private final Integer totalTaskCount;
  private final Integer logisticsTaskCount;
  private final Integer inventoryTaskCount;
  private final Integer totalDelayedCount;
  private final Integer logisticsDelayedCount;
  private final Integer inventoryDelayedCount;

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
      .predShipmentLeadTime(s.getPredShipmentLeadTime())
      .predTurnOverRate(s.getPredTurnOverRate())
      .totalTaskCount(s.getTotalTaskCount())
      .logisticsTaskCount(s.getLogisticsTaskCount())
      .inventoryTaskCount(s.getInventoryTaskCount())
      .totalDelayedCount(s.getTotalDelayedCount())
      .logisticsDelayedCount(s.getLogisticsDelayedCount())
      .inventoryDelayedCount(s.getInventoryDelayedCount())
      .build();
  }
}
