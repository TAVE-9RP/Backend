package com.nexerp.domain.company.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyLogoUploadResponse {

  private final String objectKey;
  private final String logoUrl;
}
