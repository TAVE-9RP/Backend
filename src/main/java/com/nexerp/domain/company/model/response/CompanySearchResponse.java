package com.nexerp.domain.company.model.response;

import com.nexerp.domain.company.model.entity.Company;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanySearchResponse {

  private final Long id;
  private final String name;
  private final String industryType;
  private final String description;
  private final String imagePath;

  // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
  public static CompanySearchResponse from(Company company) {
    return CompanySearchResponse.builder()
        .id(company.getId())
        .name(company.getName())
        .industryType(company.getIndustryType())
        .description(company.getDescription())
        .imagePath(company.getImagePath())
        .build();
  }

}
