package com.nexerp.domain.kpi.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ShipmentLeadTimeChartResponse {
  private Long companyId;
  private Integer year;
  private List<MonthValue> history;

  @Getter
  @AllArgsConstructor
  public static class MonthValue {
    private String month;
    private Double value;
  }
}
