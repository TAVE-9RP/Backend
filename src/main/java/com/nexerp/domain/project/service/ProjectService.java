package com.nexerp.domain.project.service;

import com.nexerp.domain.admin.service.AdminService;
import com.nexerp.domain.company.model.entity.Company;
import com.nexerp.domain.company.service.CompanyService;
import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.repository.MemberRepository;
import com.nexerp.domain.member.service.MemberService;
import com.nexerp.domain.project.model.entity.Project;
import com.nexerp.domain.project.model.request.ProjectCreateRequest;
import com.nexerp.domain.project.model.response.AssignListResponse;
import com.nexerp.domain.project.model.response.ProjectCreateResponse;
import com.nexerp.domain.project.model.response.ProjectDetailResponse;
import com.nexerp.domain.project.model.response.ProjectSearchResponse;
import com.nexerp.domain.project.repository.ProjectRepository;
import com.nexerp.domain.projectmember.model.entity.ProjectMember;
import com.nexerp.domain.projectmember.model.response.MemberIdNameResponseDto;
import com.nexerp.domain.projectmember.repository.ProjectMemberRepository;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

  private final AdminService adminService;
  private final MemberService memberService;
  private final CompanyService companyService;
  private final ProjectRepository projectRepository;
  private final ProjectMemberRepository projectMemberRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public ProjectCreateResponse createProject(Long ownerId,
    ProjectCreateRequest request) {

    // 회사 생성 요청 검증
    Company targetCompany = validateCreateProject(ownerId, request);

    Project newProject = Project.create(
      targetCompany,
      request.getProjectNumber(),
      request.getProjectName(),
      request.getProjectDescription(),
      request.getProjectCustomer(),
      request.getProjectExpectedEndDate()
    );

    Project savedProject = projectRepository.save(newProject);

    // 담당자 지정 추가
    if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {

      List<Member> assignees = adminService.getMembersByIdsAndCompany(request.getAssigneeIds(),
        targetCompany.getId());
      // ProjectMember 생성
      for (Member m : assignees) {
        ProjectMember pm = ProjectMember.create(savedProject, m);
        savedProject.getProjectMembers().add(pm);
      }
    }

    return ProjectCreateResponse.from(savedProject.getId());
  }

  @Transactional(readOnly = true)
  public Company validateCreateProject(Long ownerId, ProjectCreateRequest request) {
    // 오너 검증
    Member owner = adminService.validateOwner(ownerId);

    // 오너의 회사 정보 조회
    Long ownerCompanyId = owner.getCompanyId();
    Company ownerCompany = companyService.getCompanyEntity(ownerCompanyId);

    // 프로젝트 생성 필드 검증
    if (projectRepository.existsByNumber(request.getProjectNumber())) {
      throw new BaseException(GlobalErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 프로젝트 번호입니다.");
    }

    return ownerCompany;
  }

  @Transactional(readOnly = true)
  public List<ProjectSearchResponse> searchProjectByName(Long memberId, String keyword) {
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);
    List<Long> ids = projectRepository.findProjectIds(memberCompanyId, keyword);

    if (ids.isEmpty()) {
      return Collections.emptyList();
    }

    List<Project> projects = projectRepository.findProjectsWithMembers(ids);

    return ProjectSearchResponse.fromList(projects);
  }

  @Transactional(readOnly = true)
  public List<AssignListResponse> getAssignListMembers(Long ownerId) {
    Member owner = adminService.validateOwner(ownerId);
    Long companyId = owner.getCompanyId();

    List<Member> approvedMembers = adminService.getApprovedMembers(companyId);

    return approvedMembers.stream()
      .map(AssignListResponse::from)
      .toList();
  }

  // 프로젝트 상세 조회
  @Transactional(readOnly = true)
  public ProjectDetailResponse getProjectDetails(Long projectId, Long memberId) {

    // 회원 정보 조회 (회원의 company_id를 얻고자)
    Member currentMember = memberRepository.findById(memberId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "회원이 존재하지 않습니다."));

    // 프로젝트 조회
    Optional<Project> projectOptional = projectRepository.findProjectDetailsById(projectId);

    // 프로젝트가 존재하지 않을 경우 예외 처리
    if (projectOptional.isEmpty()) {
      throw new BaseException(GlobalErrorCode.NOT_FOUND, "프로젝트가 존재하지 않습니다.");
    }

    Project project = projectOptional.get();

    // 프로젝트가 속한 회사의 직원이 아닐 경우 예외 처리
    if (!project.getCompany().getId().equals(currentMember.getCompanyId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "해당 프로젝트의 회사 직원이 아닙니다.");
    }

    return ProjectDetailResponse.builder()
      .projectNumber(project.getNumber())
      .projectTitle(project.getTitle())
      .description(project.getDescription())
      .customer(project.getCustomer())
      .expectedEndDate(project.getExpectedEndDate())
      .endDate(project.getEndDate())
      .createDate(project.getCreateDate())
      .projectMembers(project.getProjectMembers().stream()
        // ProjectMember (pm)에서 Member 엔티티를 가져옵니다.
        .map(pm -> pm.getMember())
        .map(member -> MemberIdNameResponseDto.builder()
          .memberId(member.getId())
          .name(member.getName())
          .build())
        .collect(Collectors.toList()))
      .build();

  }

}
