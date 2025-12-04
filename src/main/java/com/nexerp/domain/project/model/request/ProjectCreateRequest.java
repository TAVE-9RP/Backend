package com.nexerp.domain.project.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectCreateRequest {

  @NotBlank(message = "프로젝트 번호를 입력하세요")
  private String projectNumber;

  @NotBlank(message = "프로젝트 이름을 입력하세요")
  private String projectName;

  @NotBlank(message = "프로젝트 설명을 작성하세요")
  private String projectDescription;

  private String projectCustomer;

  @JsonFormat(pattern = "yyyy-MM-dd")
  @NotNull(message = "목표 종료일을 입력하세요")
  private LocalDate projectExpectedEndDate;
}
