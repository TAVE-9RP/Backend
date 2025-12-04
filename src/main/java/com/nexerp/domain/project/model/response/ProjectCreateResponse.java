package com.nexerp.domain.project.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectCreateResponse {

  private final Long projectId;

  public static ProjectCreateResponse from(Long projectId) {
    return ProjectCreateResponse
      .builder()
      .projectId(projectId)
      .build();
  }
}
