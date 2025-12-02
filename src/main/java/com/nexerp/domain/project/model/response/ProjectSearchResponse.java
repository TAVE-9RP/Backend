package com.nexerp.domain.project.model.response;

import com.nexerp.domain.project.model.entity.Project;
import com.nexerp.domain.project.model.enums.ProjectStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectSearchResponse {

  private final Long projectId;

  private final String projectNumber;

  private final String projectName;

  private String projectDescription;

  private final String projectCustomer;

  private final LocalDateTime projectCreateDate;

  // 담장자를 보여줘야 하지만 지금은 관계 표현이 없음으로 추후 추가

  private final ProjectStatus status;

  public static ProjectSearchResponse from(Project project) {
    return ProjectSearchResponse.builder()
      .projectId(project.getId())
      .projectNumber(project.getNumber())
      .projectName(project.getName())
      .projectDescription(project.getDescription())
      .projectCustomer(project.getCustomer())
      .projectCreateDate(project.getCreateDate())
      .status(project.getStatus())
      .build();
  }

  public static List<ProjectSearchResponse> fromList(List<Project> projects) {
    return projects.stream()
      .map(ProjectSearchResponse::from)
      .collect(Collectors.toList());
  }
}
