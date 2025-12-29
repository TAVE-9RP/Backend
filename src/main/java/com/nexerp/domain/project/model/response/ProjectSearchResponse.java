package com.nexerp.domain.project.model.response;

import com.nexerp.domain.project.model.entity.Project;
import com.nexerp.domain.project.model.enums.ProjectStatus;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
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

  private final String projectTitle;

  private String projectDescription;

  private final String projectCustomer;

  private final LocalDateTime projectCreateDate;

  private final String projectMembers;

  private final ProjectStatus status;

  public static ProjectSearchResponse from(Project project) {

    List<String> memberNames = project.getProjectMembers().stream()
      .map(pm -> pm.getMember().getName())
      .toList();

    String assigneeSummary;
    if (memberNames.isEmpty()) {
      throw new BaseException(GlobalErrorCode.NOT_FOUND, "프로젝트 담당자를 찾을 수 없습니다.");
    } else if (memberNames.size() == 1) {
      assigneeSummary = memberNames.get(0);
    } else {
      assigneeSummary = memberNames.get(0) + " 외 " + (memberNames.size() - 1) + "명";
    }

    return ProjectSearchResponse.builder()
      .projectId(project.getId())
      .projectNumber(project.getNumber())
      .projectTitle(project.getTitle())
      .projectDescription(project.getDescription())
      .projectCustomer(project.getCustomer())
      .projectCreateDate(project.getCreateDate())
      .projectMembers(assigneeSummary)
      .status(project.getStatus())
      .build();
  }

  public static List<ProjectSearchResponse> fromList(List<Project> projects) {
    return projects.stream()
      .map(ProjectSearchResponse::from)
      .collect(Collectors.toList());
  }
}
