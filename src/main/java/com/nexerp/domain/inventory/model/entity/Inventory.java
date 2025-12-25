package com.nexerp.domain.inventory.model.entity;

import com.nexerp.domain.inventoryitem.model.entity.InventoryItem;
import com.nexerp.domain.project.model.entity.Project;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "inventory_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Column(name = "inventory_title")
  private String title;

  @Column(name = "inventory_description")
  private String description;

  // 출하를 요너에게 요청한 일
  @Column(name = "inventory_requested_at")
  private LocalDateTime requestedAt;

  @Column(name = "inventory_completed_at")
  private LocalDateTime completedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "inventory_status", nullable = false)
  private TaskStatus status;

  // 업무 생성일
  @Column(name = "inventory_created_at")
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InventoryItem> inventoryItems = new ArrayList<>();

  @Builder
  public Inventory(Project project,
    String title,
    String description,
    LocalDateTime requestedAt,
    LocalDateTime completedAt,
    TaskStatus status,
    LocalDateTime createdAt) {
    this.project = project;
    this.title = title;
    this.description = description;
    this.requestedAt = requestedAt;
    this.completedAt = completedAt;
    this.status = status;
    this.createdAt = createdAt;
  }

  // 프로젝트 생성 시 자동 상태 ASSIGNED
  public static Inventory assign(Project project) {
    return Inventory.builder()
      .project(project)
      .status(TaskStatus.ASSIGNED)
      .createdAt(LocalDateTime.now())
      .build();
  }

  public void updateCommonInfo(String title, String description) {
    this.title = title;
    this.description = description;
  }

  public void updateStatus(TaskStatus status, LocalDateTime time) {
    this.status = status;

    // 승인 요청(PENDING) 시 요청일 갱신
    if (status == TaskStatus.PENDING) {
      this.requestedAt = time;
    }

    // 업무 종료(COMPLETED) 시 완료일 저장
    if (status == TaskStatus.COMPLETED) {
      this.completedAt = time;
    }
  }

  public void reject() {
    this.requestedAt = null;
    updateStatus(TaskStatus.ASSIGNED, null);
  }
}
