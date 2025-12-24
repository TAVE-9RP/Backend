package com.nexerp.domain.logistics.model.response;

import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.global.common.model.TaskStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogisticsDetailsResponse {

  private final String projectNumber;
  private final List<String> logisticsAssignees;
  private final String logisticsTitle;
  private final String logisticsDescription;
  private final String logisticsCarrier;
  private final String logisticsCarrierCompany;
  private final LocalDate logisticsRequestedAt;
  private final TaskStatus logisticsStatus;

  public static LogisticsDetailsResponse from(Logistics logistics, List<String> assignees) {
    return LogisticsDetailsResponse.builder()
      .projectNumber(logistics.getProject().getNumber())
      .logisticsAssignees(assignees)
      .logisticsTitle(logistics.getTitle())
      .logisticsDescription(logistics.getDescription())
      .logisticsCarrier(logistics.getCarrier())
      .logisticsCarrierCompany(logistics.getCarrierCompany())
      .logisticsRequestedAt(logistics.getRequestedAt())
      .logisticsStatus(logistics.getStatus())
      .build();
  }

}
