package com.nexerp.domain.project.controller;

import com.nexerp.domain.project.model.request.ProjectCreateRequest;
import com.nexerp.domain.project.model.response.AssignListResponse;
import com.nexerp.domain.project.model.response.ProjectCreateResponse;
import com.nexerp.domain.project.model.response.ProjectDetailResponse;
import com.nexerp.domain.project.model.response.ProjectSearchResponse;
import com.nexerp.domain.project.service.ProjectService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.config.SwaggerConfig;
import com.nexerp.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "프로젝트 관련 API", description = "생성 / 키워드를 통한 조회(리스트) 등")
@SecurityRequirement(name = SwaggerConfig.AT_SCHEME)
public class ProjectController {

  private final ProjectService projectService;

  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @PostMapping
  @Operation(
    summary = "프로젝트 생성 api",
    description = "**오너 로그인이 되어 있어야 합니다.** "
      + "프로젝트 번호는 중복 불가합니다. "
      + "`프로젝트 번호, 이름, 설명, 목표 종료일`은 필수입니다."
      + "날짜의 형식은 yyyy-mm-dd입니다."
      + "담당자는 반드시 1명 이상 지정해야 합니다.",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "추가 입력 정보",
      required = true,
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ProjectCreateRequest.class),
        examples = @ExampleObject(
          name = "프로젝트 생성 예시",
          value = """
            {
              "projectNumber": "PRO_1",
              "projectName": "프로젝트 이름",
              "projectDescription":"이원진의 간절한 프로젝트입니다.",
              "projectTaskDescription": "이원진님 코드 짜세요",
              "projectCustomer":"wonjin",
              "projectExpectedEndDate":"2025-01-02",
              "assigneeIds": [1, 2, 3]
            }
            """
        )
      )
    )
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = BaseResponse.class),
        examples = @ExampleObject(
          name = "성공 예시",
          value = """
            {
                  "timestamp": "2025-12-26T18:14:23.244638700Z",
                  "isSuccess": true,
                  "status": 200,
                  "code": "SUCCESS",
                  "message": "요청에 성공했습니다.",
                  "result": {
                          "projectId": 4
                      }
              }
            """
        )
      )
    )
  })
  public BaseResponse<ProjectCreateResponse> createProject(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody ProjectCreateRequest projectCreateRequest) {

    Long ownerId = userDetails.getMemberId();
    ProjectCreateResponse projectCreateResponse = projectService.createProject(ownerId,
      projectCreateRequest);

    return BaseResponse.success(projectCreateResponse);
  }

  @GetMapping
  @Operation(summary = "키워드를 통한 프로젝트 조회(프로젝트 넘버 / 이름) api",
    description = " **keyword 파라미터 필수** keyword=\"\"의 경우 모든 프로젝트 반환 "
      + "/ 키워드 포함 프로젝트가 없는 경우 빈리스트 반환"
  )
  public BaseResponse<List<ProjectSearchResponse>> searchProject(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestParam("keyword") String keyword) {
    Long userId = userDetails.getMemberId();
    List<ProjectSearchResponse> result = projectService.searchProjectByName(userId, keyword);
    return BaseResponse.success(result);
  }


  @GetMapping("/{projectId}")
  @Operation(summary = "프로젝트 상세 조회 api",
    description = "프로젝트 번호, 회원 정보 필수"
  )
  public BaseResponse<ProjectDetailResponse> getProjectDetails(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long projectId) {
    Long memberId = userDetails.getMemberId();

    ProjectDetailResponse result = projectService.getProjectDetails(projectId, memberId);

    return BaseResponse.success(result);
  }

  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @GetMapping("/assign-members")
  @Operation(summary = "담당자 할당을 위한 직원 조회 API", description =
    "프로젝트 생성 시 담당자 할당을 위해 승인 완료된 직원의 부서명과 이름을 리턴합니다. " +
      "")
  public BaseResponse<List<AssignListResponse>> getApprovedMembers(
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long ownerId = userDetails.getMemberId();
    List<AssignListResponse> result = projectService.getAssignListMembers(ownerId);
    return BaseResponse.success(result);
  }

  @GetMapping("/assigned")
  @Operation(summary = "담당자 본인에게 할당된 프로젝트 리스트 조회 api"
  )
  public BaseResponse<List<ProjectSearchResponse>> findProjectsByMemberId(
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getMemberId();

    List<ProjectSearchResponse> result = projectService.findProjectsByMemberId(memberId);

    return BaseResponse.success(result);
  }

  @PreAuthorize("hasPermission('MANAGEMENT', 'ALL')")
  @GetMapping("/serial-num")
  @Operation(summary = "신규 프로젝트 번호 생성 api"
  )
  public BaseResponse<String> createNewProjectNum(
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getMemberId();

    String result = projectService.createNewProjectNum(memberId);

    return BaseResponse.success(result);
  }

}
