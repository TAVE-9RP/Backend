package com.nexerp.domain.kpi.model.entity;

import com.nexerp.domain.kpi.model.response.IntegratedKpiResponse;
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
  private Double longTermTaskRate;
  private Double safetyStockRate;
  private Double turnOverRate;
  private Double shipmentLeadTimeAvg;
  private Double shippingCompletionRate;
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

  // 통합 업데이트 메서드
  public void updateAllMetrics(IntegratedKpiResponse.Metrics metrics, LocalDateTime calculatedAt) {
    this.safetyStockRate = metrics.getSafetyStockRate();
    this.shipmentLeadTimeAvg = metrics.getShipmentLeadTimeAvg();
    this.shippingCompletionRate = metrics.getShippingCompletionRate();
    this.projectCompletionRate = metrics.getProjectCompletionRate();
    this.longTermTaskRate = metrics.getLongTermTaskRate();
    this.turnOverRate = metrics.getTurnOverRate();
    this.calculatedAt = calculatedAt;
  }


}
