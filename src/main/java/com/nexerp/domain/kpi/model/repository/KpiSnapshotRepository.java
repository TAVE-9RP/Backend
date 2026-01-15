package com.nexerp.domain.kpi.model.repository;

import com.nexerp.domain.kpi.model.entity.KpiSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface KpiSnapshotRepository extends JpaRepository<KpiSnapshot, Long> {
  Optional<KpiSnapshot> findByCompanyIdAndSnapshotDate(Long companyId, LocalDate snapshotDate);

  // 특정 회사의 데이터 중 날짜 기준 내림차순으로 가장 첫 번째(최신) 데이터 조회
  Optional<KpiSnapshot> findFirstByCompanyIdOrderBySnapshotDateDesc(Long companyId);

  List<KpiSnapshot> findAllByCompanyIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
    Long companyId,
    LocalDate startDate,
    LocalDate endDate
  );
}
