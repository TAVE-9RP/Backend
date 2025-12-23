package com.nexerp.domain.inventory.model.entity;

import com.nexerp.domain.inventory.model.enums.InventoryStatus;
import com.nexerp.domain.inventoryitem.model.entity.InventoryItem;
import com.nexerp.domain.project.model.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "inventory_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
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
  private InventoryStatus status;

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
                   InventoryStatus status,
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
      .status(InventoryStatus.ASSIGNED)
      .createdAt(LocalDateTime.now())
      .build();
  }

  public void updateCommonInfo(String title, String description, LocalDateTime requestedAt) {
    this.title = title;
    this.description = description;
    this.requestedAt = requestedAt;
  }

  public void updateStatus(InventoryStatus status, LocalDateTime time) {
    this.status = status;

    // 승인 요청(PENDING) 시 요청일 갱신
    if (status == InventoryStatus.PENDING) {
      this.requestedAt = time;
    }

    // 업무 종료(COMPLETED) 시 완료일 저장
    if (status == InventoryStatus.COMPLETED) {
      this.completedAt = time;
    }
  }
}
