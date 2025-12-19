package com.nexerp.domain.logistics.model.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogisticsUpdateRequest {

  private String logisticsTitle;

  private String logisticsDescription;

  private String logisticsCarrier;

  private String logisticsCarrierCompany;

}
