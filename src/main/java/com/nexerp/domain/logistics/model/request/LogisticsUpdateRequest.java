package com.nexerp.domain.logistics.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogisticsUpdateRequest {

  @NotBlank(message = "출하 업무명을 입력하세요.")
  private String logisticsTitle;

  @NotBlank(message = "업무 설명을 입력하세요.")
  private String logisticsDescription;

  private String logisticsCarrier;

  private String logisticsCarrierCompany;

}
