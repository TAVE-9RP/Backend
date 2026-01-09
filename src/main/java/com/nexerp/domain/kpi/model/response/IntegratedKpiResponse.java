package com.nexerp.domain.kpi.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class IntegratedKpiResponse {
  private Long companyId;
  private String snapshotDate;
  private Metrics metrics;
  private String calculatedAt;

  @Getter
  @Builder
  @AllArgsConstructor
  public static class Metrics {
    private Double safetyStockRate;
    private Double shipmentLeadTimeAvg;
    private Double shippingCompletionRate;
    private Double projectCompletionRate;
    private Double longTermTaskRate;
    private Double turnOverRate;
  }
}
