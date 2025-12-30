package com.nexerp.domain.admin.service;

import com.nexerp.domain.admin.model.request.JoinStatusUpdateRequest;
import com.nexerp.domain.admin.model.request.JoinStatusUpdateRequest.StatusUpdateUnit;
import com.nexerp.domain.admin.model.request.PermissionUpdateRequest;
import com.nexerp.domain.admin.model.request.PermissionUpdateRequest.PermissionUpdateUnit;
import com.nexerp.domain.admin.model.response.JoinStatusResponse;
import com.nexerp.domain.admin.model.response.PermissionResponse;
import com.nexerp.domain.admin.repository.AdminRepository;
import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.domain.inventory.repository.InventoryRepository;
import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.domain.logistics.repository.LogisticsRepository;
import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import com.nexerp.domain.member.model.enums.MemberRole;
import com.nexerp.domain.member.util.EnumValidatorUtil;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.common.model.TaskStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final AdminRepository adminRepository;
  private final InventoryRepository inventoryRepository;
  private final LogisticsRepository logisticsRepository;

  // 로그인한 OWNER가 속한 회사의 모든 직원 (요청/승인/거절 포함) 조회
  @Transactional(readOnly = true)
  public List<JoinStatusResponse> getMemberJoinStatus(Long ownerId) {

    Member owner = adminRepository.findById(ownerId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "로그인한 회원 정보를 찾을 수 없습니다."));

    Long companyId = owner.getCompanyId();

    List<Member> members = adminRepository.findByCompanyIdAndIdNotOrderByJoinRequestDateAsc(
      companyId, ownerId);

    return members.stream()
      .map(JoinStatusResponse::from)
      .toList();
  }

  // 직원 가입 상태 변경 (PENDING / APPROVED / REJECTED 간 전환)
  @Transactional
  public void changeMemberRequestStatus(
    Long ownerId,
    JoinStatusUpdateRequest requests
  ) {
    // 오너 검증
    Member owner = adminRepository.findById(ownerId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "로그인한 회원 정보를 찾을 수 없습니다."));

    if (owner.getPosition() != MemberPosition.OWNER) {
      throw new BaseException(GlobalErrorCode.UNAUTHORIZED, "직원 가입 요청 관리는 회사 오너만 사용할 수 있습니다.");
    }

    Long companyId = owner.getCompanyId();

    // 업데이트 대상 리스트 추출
    List<StatusUpdateUnit> updates = requests.getUpdates();

    // 대상 memberId 리스트 추출
    List<Long> targetIds = updates.stream()
      .map(StatusUpdateUnit::getMemberId)
      .toList();

    // 같은 회사 소속 직원들만
    List<Member> members = adminRepository.findByIdInAndCompanyId(targetIds, companyId);

    if (members.size() != targetIds.size()) {
      throw new BaseException(
        GlobalErrorCode.BAD_REQUEST,
        "요청한 직원 중 해당 회사에 속하지 않는 직원이 포함되어 있습니다."
      );
    }

    // id -> Member 매핑
    Map<Long, Member> memberMap = members.stream()
      .collect(Collectors.toMap(Member::getId, m -> m));

    // 각각 상태 변경

    for (StatusUpdateUnit unit : updates) {
      Member member = memberMap.get(unit.getMemberId());

      if (member == null) {
        throw new BaseException(
          GlobalErrorCode.NOT_FOUND,
          "직원 ID " + unit.getMemberId() + "를 찾을 수 없습니다."
        );
      }

      MemberRequestStatus newStatus = EnumValidatorUtil.validateRequestStatus(unit.getNewStatus());
      member.changeRequestStatus(newStatus);
    }

  }

  @Transactional(readOnly = true)
  public List<PermissionResponse> getMemberPermission(Long ownerId) {

    Member owner = adminRepository.findById(ownerId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "로그인한 회원 정보를 찾을 수 없습니다."));

    Long companyId = owner.getCompanyId();

    List<Member> members = adminRepository.findByCompanyIdAndRequestStatusAndPositionNot(
      companyId,
      MemberRequestStatus.APPROVED,
      MemberPosition.OWNER
    );

    return members.stream()
      .map(PermissionResponse::from)
      .toList();
  }

  @Transactional
  public void updateMemberPermissions(Long ownerId, PermissionUpdateRequest request) {

    Member owner = adminRepository.findById(ownerId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "로그인한 회원 정보를 찾을 수 없습니다."));

    if (owner.getPosition() != MemberPosition.OWNER) {
      throw new BaseException(GlobalErrorCode.UNAUTHORIZED, "직원 권한 변경은 회사 오너만 사용할 수 있습니다.");
    }

    Long companyId = owner.getCompanyId();

    // 업데이트 대상 리스트
    List<PermissionUpdateUnit> updates = request.getUpdates();

    // 대상 id 리스트
    List<Long> targetIds = updates.stream()
      .map(PermissionUpdateUnit::getMemberId)
      .toList();

    // 회사 소속 직원 조회
    List<Member> members =
      adminRepository.findByIdInAndCompanyId(targetIds, companyId);

    if (members.size() != targetIds.size()) {
      throw new BaseException(
        GlobalErrorCode.BAD_REQUEST,
        "요청한 직원 중 회사 소속이 아닌 직원이 포함되어 있습니다."
      );
    }

    // Member 매핑
    Map<Long, Member> memberMap = members.stream()
      .collect(Collectors.toMap(Member::getId, m -> m));

    // 결과 리스트
    List<PermissionResponse> responses = new ArrayList<>();

    for (PermissionUpdateUnit unit : updates) {

      Member member = memberMap.get(unit.getMemberId());

      if (member == null) {
        throw new BaseException(
          GlobalErrorCode.NOT_FOUND,
          "직원 ID " + unit.getMemberId() + "를 찾을 수 없습니다."
        );
      }

      // 오너는 변경 불가
      if (member.getPosition() == MemberPosition.OWNER) {
        continue;
      }

      MemberDepartment department = member.getDepartment();

      // ENUM 검증
      MemberRole newRole = EnumValidatorUtil.validateRole(unit.getNewRole());

      // 권한 업데이트
      member.getPermissions().updateRole(
        department,
        newRole
      );

      responses.add(PermissionResponse.from(member));
    }
  }

  @Transactional
  public void approveInventory(Long ownerId, Long inventoryId) {

    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    validateOwner(ownerId);

    inventory.approve();
  }

  // 오너인지 검증 후 오너인 경우 오너 반환
  @Transactional(readOnly = true)
  public Member validateOwner(Long ownerId) {
    Member owner = adminRepository.findById(ownerId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "로그인한 회원 정보를 찾을 수 없습니다."));

    if (owner.getPosition() != MemberPosition.OWNER) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "해당 기능은 회사 오너만 사용할 수 있습니다.");
    }

    return owner;
  }

  // 담당자 검증용 메서드
  @Transactional(readOnly = true)
  public List<Member> getMembersByIdsAndCompany(List<Long> memberIds, Long companyId) {

    if (memberIds == null || memberIds.isEmpty()) {
      throw new BaseException(
        GlobalErrorCode.VALIDATION_ERROR,
        "담당자는 최소 1명 이상 지정해야 합니다."
      );
    }

    List<Member> members = adminRepository.findByIdInAndCompanyId(memberIds, companyId);
    // 요청한 모든 직원이 같은 회사 소속인지 검증
    if (members.size() != memberIds.size()) {
      throw new BaseException(
        GlobalErrorCode.BAD_REQUEST,
        "요청한 담당자 중 회사 소속이 아닌 직원이 포함되어 있습니다."
      );
    }

    return members;
  }

  // 승인된 직원만 리턴
  @Transactional(readOnly = true)
  public List<Member> getApprovedMembers(Long companyId) {
    return adminRepository.findByCompanyIdAndRequestStatusAndPositionNot(
      companyId,
      MemberRequestStatus.APPROVED,
      MemberPosition.OWNER
    );
  }

  @Transactional
  public void rejectInventory(Long ownerId, Long inventoryId) {

    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    validateOwner(ownerId);

    if (inventory.getStatus() != TaskStatus.PENDING) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "PENDING 상태에서만 거절할 수 있습니다.");
    }

    inventory.reject();
  }

  @Transactional
  public void rejectLogistics(Long ownerId, Long logisticsId) {
    Logistics logistics = logisticsRepository.findById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));
    validateOwner(ownerId);

    if (logistics.getStatus() != TaskStatus.PENDING) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "PENDING 상태에서만 거절할 수 있습니다.");
    }

    logistics.reject();
  }
}
