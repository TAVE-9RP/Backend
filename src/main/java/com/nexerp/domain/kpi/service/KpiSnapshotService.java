package com.nexerp.domain.kpi.service;

import com.nexerp.domain.kpi.model.entity.KpiSnapshot;
import com.nexerp.domain.kpi.model.repository.KpiSnapshotRepository;
import com.nexerp.domain.kpi.model.response.IntegratedKpiResponse;
import com.nexerp.domain.kpi.model.response.KpiDashboardResponse;
import com.nexerp.domain.member.service.MemberService;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KpiSnapshotService {

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
}
