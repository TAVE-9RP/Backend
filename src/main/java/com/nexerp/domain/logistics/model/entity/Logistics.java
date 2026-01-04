package com.nexerp.domain.logistics.model.entity;

import com.nexerp.domain.logisticsItem.model.entity.LogisticsItem;
import com.nexerp.domain.project.model.entity.Project;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.common.model.TaskProcessingStatus;
import com.nexerp.global.common.model.TaskStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "logistics")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Logistics {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "logistics_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;

  @OneToMany(mappedBy = "logistics",
    cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<LogisticsItem> logisticsItems = new ArrayList<>();

  @Column(name = "logistic_title")
  private String title;

  @Column(name = "logistics_description")
  private String description;

  // 담당자가 관리자에게 승인 요청한 시간
  @Column(name = "logistics_requested_at")
  private LocalDate requestedAt;

  // 모든 출하 물품 완료 후 완료 처리 시
  @Column(name = "logistics_completed_at")
  private LocalDateTime completedAt;

  // (업무할당/승인대기/ 진행 중 / 출하 완료 ) 완료 로그 값
  @Column(name = "logistics_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private TaskStatus status;

  // 관리자로 인한 생성 시각
  @Column(name = "logistic_created_at")
  private LocalDateTime createdAt;

  // 운송 수단
  @Column(name = "logistic_carrier")
  private String carrier;

  //운송업체
  @Column(name = "logistic_carrier_company")
  private String carrierCompany;

  public static Logistics assign(Project project) {
    return Logistics.builder()
      .project(project)
      .status(TaskStatus.ASSIGNED)
      .createdAt(LocalDateTime.now())
      .build();

  }

  public void update(String logisticsTitle, String logisticsCarrier,
    String logisticsCarrierCompany, String logisticsDescription) {
    if (logisticsTitle != null) {
      this.title = logisticsTitle;
    }
    if (logisticsCarrier != null) {
      this.carrier = logisticsCarrier;
    }
    if (logisticsCarrierCompany != null) {
      this.carrierCompany = logisticsCarrierCompany;
    }
    if (logisticsDescription != null) {
      this.description = logisticsDescription;
    }
  }

  public void changeStatus(TaskStatus status) {
    this.status = status;
  }

  public void requestApproval() {

    if (this.status != TaskStatus.ASSIGNED && this.status != TaskStatus.REJECT) {
      throw new BaseException(GlobalErrorCode.STATE_CONFLICT, "할당 단계 또는 반려에서 승인 요청이 가능합니다.");
    }

    this.requestedAt = LocalDate.now();
    changeStatus(TaskStatus.PENDING);
  }

  public void approve() {
    if (this.status != TaskStatus.PENDING) {
      throw new BaseException(GlobalErrorCode.STATE_CONFLICT, "승인 대기에서만 승인 가능합니다.");
    }

    changeStatus(TaskStatus.IN_PROGRESS);
    if (this.project != null) {
      this.project.start();
    }
  }

  public void complete() {
    if (this.status != TaskStatus.IN_PROGRESS) {
      throw new BaseException(GlobalErrorCode.STATE_CONFLICT, "진행 중에서만 완료 가능합니다.");
    }

    boolean allItemsCompleted = this.logisticsItems.stream()
      .allMatch(item -> item.getProcessingStatus() == TaskProcessingStatus.COMPLETED);

    if (!allItemsCompleted) {
      throw new BaseException(GlobalErrorCode.STATE_CONFLICT, "모든 물품의 출하 처리가 완료되지 않았습니다.");
    }

    this.completedAt = LocalDateTime.now();
    changeStatus(TaskStatus.COMPLETED);
  }

  public void reject() {
    this.requestedAt = null;
    changeStatus(TaskStatus.REJECT);
  }
}
