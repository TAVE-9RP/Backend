package com.nexerp.domain.project.controller;

import com.nexerp.domain.project.model.request.ProjectCreateRequest;
import com.nexerp.domain.project.model.response.ProjectCreateResponse;
import com.nexerp.domain.project.service.ProjectService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.details.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

  private final ProjectService projectService;

  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @PostMapping
  public BaseResponse<ProjectCreateResponse> createProject(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody ProjectCreateRequest projectCreateRequest) {

    Long ownerId = userDetails.getMemberId();
    ProjectCreateResponse projectCreateResponse = projectService.createProject(ownerId,
      projectCreateRequest);

    return BaseResponse.success(projectCreateResponse);
  }
}
