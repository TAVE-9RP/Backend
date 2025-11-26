package com.nexerp.domain.company.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "company")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Company {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "company_id")
  private Long id;

  @Column(name = "company_name", nullable = false, unique = true)
  private String name;

  // 회사 업종
  @Column(name = "company_industry_type", nullable = false)
  private String industryType;

  // 회사에 대한 설명
  @Column(name = "company_description")
  private String description;

  // 회사 로고 이미지
  @Column(name = "company_image_path")
  private String imagePath;

  // 생성 시간
  @CreatedDate
  @Column(name = "company_created_at", updatable = false)
  private LocalDateTime createdAt;

  // 수정 시간
  @LastModifiedDate
  @Column(name = "company_updated_at")
  private LocalDateTime updatedAt;

  // 회사 생성에 사용
  public static Company create(String name, String industryType, String description,
      String imagePath) {
    return Company.builder()
        .name(name)
        .industryType(industryType)
        .description(description)
        .imagePath(imagePath)
        .build();
  }
}
