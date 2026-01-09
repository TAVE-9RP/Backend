package com.nexerp.domain.analytics.scheduler;

import com.nexerp.domain.analytics.application.AnalyticsExportOrchestrator;
import java.time.LocalDate;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsExportScheduler {

  private final AnalyticsExportOrchestrator orchestrator;

  // 매일 새벽 2시
  @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
  public void runDaily() {
    // 데이터가 어제  이기 때문에 어제 날짜
    LocalDate date = LocalDate.now().minusDays(1);
    try {
      log.info("[AnalyticsExport] Scheduled start date={}", date);
      orchestrator.exportAllFailFastParallel(date);
      log.info("[AnalyticsExport] Scheduled success date={}", date);
    } catch (Exception e) {
      // 실패하면 전체 실패(fail-fast)로 끝나므로 여기서 알람/로그 처리
      log.error("[AnalyticsExport] Scheduled failed date={}", date, e);
      throw e; // 원하면 swallow(무시)해도 되지만, 보통은 로그만 남기고 끝냄
    }
  }
}
