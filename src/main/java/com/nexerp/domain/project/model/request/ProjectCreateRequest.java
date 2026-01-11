package com.nexerp.domain.project.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
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

  @NotBlank(message = "프로젝트 업무 설명을 작성하세요")
  private String projectTaskDescription;

  private String projectCustomer;

  @JsonFormat(pattern = "yyyy-MM-dd")
  @NotNull(message = "목표 종료일을 입력하세요")
  private LocalDate projectExpectedEndDate;

  // 프로젝트 담당자 리스트
  @NotEmpty(message = "담당자를 최소 1명 이상 지정해주세요.")
  private List<Long> assigneeIds;
}
