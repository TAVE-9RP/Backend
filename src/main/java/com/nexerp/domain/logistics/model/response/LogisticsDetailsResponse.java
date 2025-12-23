package com.nexerp.domain.logistics.model.response;

import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.domain.logistics.model.enums.LogisticsSatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogisticsDetailsResponse {

  private final String projectNumber;

  private final String logisticsTitle;

  private final String logisticsDescription;

  private final String logisticsCarrier;

  private final String logisticsCarrierCompany;

  private final LocalDate logisticsRequestedAt;

  private final LocalDateTime logisticsCompletedAt;

  private final LogisticsSatus logisticsSatus;

  public static LogisticsDetailsResponse from(Logistics logistics) {
    return LogisticsDetailsResponse.builder()
      .projectNumber(logistics.getProject().getNumber())
      .logisticsTitle(logistics.getTitle())
      .logisticsDescription(logistics.getDescription())
      .logisticsCarrier(logistics.getCarrier())
      .logisticsCarrierCompany(logistics.getCarrierCompany())
      .logisticsRequestedAt(logistics.getRequestedAt())
      .logisticsCompletedAt(logistics.getCompletedAt())
      .logisticsSatus(logistics.getStatus())
      .build();
  }

}
