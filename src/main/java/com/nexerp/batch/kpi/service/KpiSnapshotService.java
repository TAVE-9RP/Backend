package com.nexerp.batch.kpi.service;

import com.nexerp.batch.kpi.model.entity.KpiSnapshot;
import com.nexerp.batch.kpi.model.repository.KpiSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KpiSnapshotService {

  private final KpiSnapshotRepository snapshotRepository;

  @Transactional
  public void saveSafetyStockResult(Long companyId, LocalDate date, Double rate) {
    KpiSnapshot snapshot = snapshotRepository.findByCompanyIdAndSnapshotDate(companyId, date)
      .orElseGet(() -> KpiSnapshot.builder()
        .companyId(companyId)
        .snapshotDate(date)
        .calculatedAt(LocalDateTime.now())
        .build());

    // 안전재고 확보율 필드만 업데이트 (다른 KPI는 보존)
    snapshot.updateSafetyStock(rate, LocalDateTime.now());

    snapshotRepository.save(snapshot);
  }
}
