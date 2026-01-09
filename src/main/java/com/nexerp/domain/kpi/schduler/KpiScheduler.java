package com.nexerp.domain.kpi.schduler;

import com.nexerp.domain.kpi.service.KpiIntegrationService;
import com.nexerp.domain.company.service.CompanyService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KpiScheduler {

  private final KpiIntegrationService kpiIntegrationService;
  private final CompanyService companyService;

  @PostConstruct
  //@Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
  public void scheduleKpiSync() {
    // 테스트용
    // 1. DB에서 활성화된 모든 회사 ID
    List<Long> allCompanyIds = companyService.getAllCompanyIds();

    // 2. 어제 날짜 데이터 기준 (Snapshot 저장 규칙)
    LocalDate targetDate = LocalDate.now().minusDays(1);

    log.info("KPI 동기화 시작: 대상 회사 {}건, 기준 날짜: {}", allCompanyIds.size(), targetDate);

    for (Long companyId : allCompanyIds) {
      try {
        kpiIntegrationService.syncIntegratedKpiFromS3(companyId, targetDate);
      } catch (Exception e) {
        log.error("회사 ID {}의 KPI 동기화 중 오류 발생: {}", companyId, e.getMessage());
      }
    }

    log.info("KPI 동기화 프로세스 완료");
  }

    // 1월 기준 테스트용
//  @PostConstruct
//  public void backfillKpiData() {
//    List<Long> companyIds = companyService.getAllCompanyIds();
//    // 발표용 타겟 날짜 설정
//    LocalDate targetDate = LocalDate.of(2025, 12, 31);
//
//    for (Long companyId : companyIds) {
//      // report_2025-12-31.json을 읽어 DB에 저장
//      kpiIntegrationService.syncIntegratedKpiFromS3(companyId, targetDate);
//    }
//  }
}
