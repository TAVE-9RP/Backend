package com.nexerp.batch.kpi.model.repository;

import com.nexerp.batch.kpi.model.entity.KpiSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface KpiSnapshotRepository extends JpaRepository<KpiSnapshot, Long> {
  // 회사 ID와 날짜로 조회 (Insert, Update)
  Optional<KpiSnapshot> findByCompanyIdAndSnapshotDate(Long companyId, LocalDate snapshotDate);
}
