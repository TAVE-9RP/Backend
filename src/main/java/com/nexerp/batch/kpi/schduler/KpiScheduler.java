package com.nexerp.batch.kpi.schduler;

import com.nexerp.batch.kpi.service.KpiIntegrationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KpiScheduler {

  private final KpiIntegrationService kpiIntegrationService;

  @PostConstruct
  // @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
  public void scheduleKpiSync() {
    // 테스트용
    List<Long> companyIds = List.of(1L);
    LocalDate targetDate = LocalDate.now();

    for (Long companyId : companyIds) {
      kpiIntegrationService.syncSafetyStockFromS3(companyId, targetDate);
    }
  }
}
