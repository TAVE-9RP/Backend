package com.nexerp.batch.kpi.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SafetyStockKpiDto {

  private Long companyId;
  private String kpiType;
  private String snapshotDate;
  private String calculatedAt;
  private KpiData data;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class KpiData {
    private Double rate;
    private Integer totalItems;
    private Integer securedItems;
  }
}
