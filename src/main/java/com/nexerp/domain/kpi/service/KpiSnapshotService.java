package com.nexerp.domain.kpi.service;

import com.nexerp.domain.kpi.model.entity.KpiSnapshot;
import com.nexerp.domain.kpi.model.repository.KpiSnapshotRepository;
import com.nexerp.domain.kpi.model.response.IntegratedKpiResponse;
import com.nexerp.domain.kpi.model.response.KpiDashboardResponse;
import com.nexerp.domain.kpi.model.response.ShipmentLeadTimeChartResponse;
import com.nexerp.domain.member.service.MemberService;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KpiSnapshotService {
  private final KpiHistoryService historyService;
  private final KpiSnapshotRepository snapshotRepository;
  private final MemberService memberService;

  @Transactional
  public void saveIntegratedResult(IntegratedKpiResponse response) {
    LocalDate date = LocalDate.parse(response.getSnapshotDate());

    KpiSnapshot snapshot = snapshotRepository.findByCompanyIdAndSnapshotDate(response.getCompanyId(), date)
      .orElseGet(() -> KpiSnapshot.builder()
        .companyId(response.getCompanyId())
        .snapshotDate(date)
        .build());

    // 모든 지표 한 번에 업데이트
    snapshot.updateAllMetrics(response.getMetrics(), LocalDateTime.parse(response.getCalculatedAt().split("\\+")[0]));

    snapshotRepository.save(snapshot);
  }

  @Transactional(readOnly=true)
  public KpiDashboardResponse getMonthlyKpi(Long memberId) {
    Long companyId = memberService.getCompanyIdByMemberId(memberId);

    // 전월 말일 계산
    LocalDate lastDayOfPrevMonth = LocalDate.now().withDayOfMonth(1).minusDays(1);

    return snapshotRepository.findByCompanyIdAndSnapshotDate(companyId, lastDayOfPrevMonth)
      .map(KpiDashboardResponse::from)
      // 만약 없으면, DB 내 해당 회사의 가장 최신 스냅샷 조회
      .orElseGet(() -> snapshotRepository.findFirstByCompanyIdOrderBySnapshotDateDesc(companyId)
        .map(KpiDashboardResponse::from)
        // 둘 다 없으면 예외 발생
        .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "해당 회사의 KPI 기록이 전무합니다."))
      );
  }

  @Transactional(readOnly=true)
  public ShipmentLeadTimeChartResponse getLeadTimeChartDate(Long memberId) {
    Long companyId = memberService.getCompanyIdByMemberId(memberId);

    LocalDate lastDayOfPrevMonth = LocalDate.now().withDayOfMonth(1).minusDays(1);
    LocalDate startOfYear = LocalDate.of(lastDayOfPrevMonth.getYear(), 1, 1);

    // DB에서 해당 범위 모든 스냅샷 조회
    List<KpiSnapshot> snapshots = snapshotRepository.findAllByCompanyIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
      companyId, startOfYear, lastDayOfPrevMonth
    );

    List<ShipmentLeadTimeChartResponse.MonthValue> chartData = snapshots.stream()
      .map(s -> new ShipmentLeadTimeChartResponse.MonthValue(
        s.getSnapshotDate().getMonthValue() + "월",
        s.getShipmentLeadTimeAvg()))
      .collect(Collectors.toList());
    return ShipmentLeadTimeChartResponse.builder()
      .companyId(companyId)
      .year(lastDayOfPrevMonth.getYear())
      .history(chartData)
      .build();
  }

  @Transactional
  public void saveHistoryFromS3(Long companyId, List<Map<String, Object>> historyData) {
    // 1. 기준 날짜 계산 (전월 말일 및 당해년도)
    LocalDate lastDayOfPrevMonth = LocalDate.now().withDayOfMonth(1).minusDays(1);
    int currentYear = lastDayOfPrevMonth.getYear();

    for (Map<String, Object> history: historyData) {
      String monthStr = (String) history.get("month");
      Double value = (Double) history.get("value");

      int month = Integer.parseInt(monthStr.replace("월", ""));

      // 2. 해당 월의 말일 계산
      LocalDate snapshotDate = LocalDate.of(currentYear, month, 1)
        .withDayOfMonth(LocalDate.of(currentYear, month, 1).lengthOfMonth());

      if (snapshotDate.isAfter(lastDayOfPrevMonth)) continue;

      if (snapshotRepository.findByCompanyIdAndSnapshotDate(companyId, snapshotDate).isEmpty()) {
        KpiSnapshot historySnapshot = KpiSnapshot.builder()
          .companyId(companyId)
          .snapshotDate(snapshotDate)
          .calculatedAt(LocalDateTime.now())
          .build();

        historySnapshot.updateShipmentLeadTimeOnly(value);
        snapshotRepository.save(historySnapshot);
      }
    }
  }
}
