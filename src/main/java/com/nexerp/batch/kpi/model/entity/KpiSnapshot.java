package com.nexerp.batch.kpi.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_snapshot", uniqueConstraints = {
  @UniqueConstraint(
    name = "uk_kpi_company_date",
    columnNames = {"companyId", "snapshotDate"}
  )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KpiSnapshot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long companyId;

  @Column(nullable = false)
  private LocalDate snapshotDate; // 일 단위 기록 Date로 설정

  // 실제 KPI 결과 값
  private Double projectCompletionRate;
  private Double taskDelayRate;
  private Double safetyStockRate;
  private Double inventoryTurnover;
  private Double avgLogisticsLeadTimeHours;

  // 예측 KPI
  private Double preInventoryTurnover;
  private Double preAvgLogisticsLeadTimeHours;

  @Column(nullable = false)
  private LocalDateTime calculatedAt;

  @Builder
  public KpiSnapshot(Long companyId, LocalDate snapshotDate, LocalDateTime calculatedAt) {
    this.companyId = companyId;
    this.snapshotDate = snapshotDate;
    this.calculatedAt = calculatedAt;
  }

  // 부분 업데이트 메서드
  public void updateSafetyStock(Double rate, LocalDateTime calculatedAt) {
    this.safetyStockRate = rate;
    this.calculatedAt = calculatedAt;
  }

  public void updateProjectKpi(Double completionRate, Double delayRate, LocalDateTime calculatedAt) {
    this.projectCompletionRate = completionRate;
    this.taskDelayRate = delayRate;
    this.calculatedAt = calculatedAt;
  }

  public void updatePredictions(Double preInventory, Double preLeadTime, LocalDateTime calculatedAt) {
    this.preInventoryTurnover = preInventory;
    this.preAvgLogisticsLeadTimeHours = preLeadTime;
  }
}
