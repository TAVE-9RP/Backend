package com.nexerp.domain.project.service;

import com.nexerp.domain.admin.service.AdminService;
import com.nexerp.domain.company.model.entity.Company;
import com.nexerp.domain.company.service.CompanyService;
import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.domain.inventory.repository.InventoryRepository;
import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.domain.logistics.repository.LogisticsRepository;
import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.response.MemberIdNameResponseDto;
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
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
  private final MemberRepository memberRepository;
  private final LogisticsRepository logisticsRepository;
  private final InventoryRepository inventoryRepository;

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

      boolean hasInventoryAssignee = assignees.stream()
        .anyMatch(a -> a.getDepartment() == MemberDepartment.INVENTORY);

      boolean hasLogisticsAssignee = assignees.stream()
        .anyMatch(a -> a.getDepartment() == MemberDepartment.LOGISTICS);

      if (hasInventoryAssignee) {
        Inventory inventory = Inventory.assign(savedProject);
        inventoryRepository.save(inventory);
      }

      // 5. 출하업무 생성 (ASSIGNED)
      if (hasLogisticsAssignee) {
        Logistics logistics = Logistics.assign(savedProject);
        logisticsRepository.save(logistics);
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

    List<MemberIdNameResponseDto> memberResponses = getProjectMembers(project);

    return ProjectDetailResponse.builder()
      .projectNumber(project.getNumber())
      .projectTitle(project.getTitle())
      .description(project.getDescription())
      .customer(project.getCustomer())
      .expectedEndDate(project.getExpectedEndDate())
      .endDate(project.getEndDate())
      .createDate(project.getCreateDate())
      .projectMembers(memberResponses)
      .build();

  }

  // 담당자 본인의 프로젝트 리스트 조회
  @Transactional(readOnly = true)
  public List<ProjectSearchResponse> findProjectsByMemberId(Long memberId) {

    // 회원 정보 조회 (회원의 company_id를 얻고자)
    Member currentMember = memberRepository.findById(memberId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "회원이 존재하지 않습니다."));

    Long companyId = currentMember.getCompanyId();

    List<Project> projects = projectRepository.findProjectsByMemberId(memberId, companyId);

    return ProjectSearchResponse.fromList(projects);

  }

  // 신규 프로젝트 번호 생성
  @Transactional(readOnly = true)
  public String createNewProjectNum(Long memberId) {

    // 회원 정보 조회 (회원의 company_id를 얻고자)
    Member currentMember = memberRepository.findById(memberId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "회원이 존재하지 않습니다."));

    Long companyId = currentMember.getCompanyId();
    String formattedCompanyId = String.format("%02d", companyId);

    // 년도(시스템 타임존) 추출
    LocalDate today = LocalDate.now();
    String formattedYear = String.valueOf(today.getYear()).substring(2);

    Optional<Project> latestProject = projectRepository.findFirstByCompanyIdOrderByCreateDateDesc(
      companyId);

    boolean isProjectEmpty = !latestProject.isPresent();

    String newProjectNumber = "";

    if (isProjectEmpty) {

      // 새로운 번호
      newProjectNumber = String.format("C%s-%s-001", formattedCompanyId, formattedYear);

    } else {

      // 기존 번호가 있는 경우
      String latestProjectNumber = latestProject.get().getNumber();
      newProjectNumber = getNextProjectNumber(formattedCompanyId, formattedYear,
        latestProjectNumber);
    }

    return newProjectNumber;
  }

  private String getNextProjectNumber(String companyId, String year, String latestProjectNumber) {

    // 접두사 부분 (C01-25)
    String prefix = String.format("C%s-%s-", companyId, year);

    // 오늘과 latestNumber의 년도 비교 후 다르면 1로 초기화
    if (latestProjectNumber == null || !latestProjectNumber.startsWith(prefix)) {
      return prefix + "001";
    }

    // 일련번호 부분 (001) 추출
    int lastHyphenIndex = latestProjectNumber.lastIndexOf('-');
    String serialNumStr = latestProjectNumber.substring(lastHyphenIndex + 1);

    // 일련번호를 정수로 변환하여 1 증가
    int currentSerialNum = Integer.parseInt(serialNumStr);
    int nextSerialNum = currentSerialNum + 1;

    // 증가된 번호를 다시 3자리 문자열로 포맷
    String formattedNextSerialNum = String.format("%03d", nextSerialNum);

    // 접두사와 새로운 일련번호 반환
    return prefix + formattedNextSerialNum;
  }

  // 회사 id를 통해 모든 프로젝트와 연관 정보 한번에 가져오기
  @Transactional(readOnly = true)
  public List<Project> getProjectsWithLogisticsByCompanyId(Long companyId) {
    List<Project> projects = projectRepository.findAllWithLogisticsAndMembersByCompanyId(companyId);
    return projects;
  }

  @Transactional(readOnly = true)
  public List<MemberIdNameResponseDto> getProjectMembers(Project project) {
    List<MemberIdNameResponseDto> memberResponses = project.getProjectMembers().stream()
      .map(pm -> new MemberIdNameResponseDto(
        pm.getMember().getId(),
        pm.getMember().getName()
      ))
      .toList();

    return memberResponses;
  }

  @Transactional
  public void completeProject(Long projectId) {
    Project project = projectRepository.findByIdWithTasks(projectId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

    project.complete();
  }
}
