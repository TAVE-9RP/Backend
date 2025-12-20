package com.nexerp.domain.logistics.model.response;

import com.nexerp.domain.logistics.model.enums.LogisticsSatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogisticsDetailsResponse {

  String projectNumber;

  String logisticsTitle;

  String logisticsDescription;

  String logisticsCarrier;

  String logisticsCarrierCompany;

  LocalDate logisticsRequestedAt;

  LocalDateTime localCompletedAt;

  LogisticsSatus logisticsSatus;

  BigDecimal logisticsTotalPrice;

}
