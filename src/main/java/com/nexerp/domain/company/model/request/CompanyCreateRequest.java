package com.nexerp.domain.company.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyCreateRequest {

  @NotBlank(message = "회사 이름을 입력하세요")
  //@Size(max=100, message = "회사 이름은 최대 100자 이내여야 합니다.")
  private String name;

  @NotBlank(message = "회사 업종을 입력하세요")
  //@Size(max=100, message = "회사 업종은 50자 이내여야 합니다.")
  private String industryType;

  @NotBlank(message = "회사 설명을 입력하세요")
  //@Size(max=100, message = "회사 설명은 200자 이내여야 합니다.")
  private String description;
}
