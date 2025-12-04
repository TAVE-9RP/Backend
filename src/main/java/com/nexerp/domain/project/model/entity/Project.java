package com.nexerp.domain.project.model.entity;

import com.nexerp.domain.company.model.entity.Company;
import com.nexerp.domain.project.model.enums.ProjectStatus;
import com.nexerp.domain.projectmember.model.entity.ProjectMember;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "project")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "project_id")
  private Long id;

  // 연결된 회사
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  //연결된 회원
  @OneToMany(mappedBy = "project", fetch = FetchType.LAZY,
    cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ProjectMember> projectMembers = new ArrayList<>();

  // 로직상 자동으로 할당 (ex: PRO_0000001)
  @Column(name = "project_number", nullable = false, updatable = false, unique = true)
  private String number;

  //오너가 지정하는 프로젝트 이름
  @Column(name = "project_name", nullable = false)
  private String name;

  //오너가 지정하는 텍스트
  @Column(name = "project_description", nullable = false)
  private String description;

  // 거래처(고객사)
  @Column(name = "project_customer")
  private String customer;

  // 미진행 / 진행 중 / 완료
  @Enumerated(EnumType.STRING)
  @Column(name = "project_status", nullable = false)
  private ProjectStatus status;

  // 오너가 지정한 목표 종료일
  @Column(name = "project_expected_end_date")
  private LocalDate expectedEndDate;

  // 프로젝트가 종료된 시간
  @Column(name = "project_end_date")
  private LocalDateTime endDate;

  // 오너가 프로젝트를 생성한 시간
  @CreatedDate
  @Column(name = "project_create_date", updatable = false)
  private LocalDateTime createDate;

  public static Project create(Company company, String number, String name, String description,
    String customer, LocalDate expectedEndDate) {
    return Project.builder()
      .company(company)
      .number(number)
      .name(name)
      .description(description)
      .customer(customer)
      .status(ProjectStatus.NOT_STARTED)
      .expectedEndDate(expectedEndDate)
      .build();
  }

  // projectMembers 지정은 민섭님 파트
}
