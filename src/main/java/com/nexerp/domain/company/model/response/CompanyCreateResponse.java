package com.nexerp.domain.company.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyCreateResponse {

  private final Long companyId;

  public static CompanyCreateResponse from(Long companyId) {
    return CompanyCreateResponse.builder()
      .companyId(companyId)
      .build();
  }
}
