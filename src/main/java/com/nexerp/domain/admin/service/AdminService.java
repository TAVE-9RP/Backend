package com.nexerp.domain.admin.service;

import com.nexerp.domain.admin.model.request.JoinStatusUpdateRequest;
import com.nexerp.domain.admin.model.response.JoinStatusResponse;
import com.nexerp.domain.admin.repository.AdminRepository;
import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import com.nexerp.domain.member.util.EnumValidatorUtil;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.hibernate.mapping.Join;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nexerp.domain.admin.model.request.JoinStatusUpdateRequest.StatusUpdateUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final AdminRepository adminRepository;

  // 로그인한 OWNER가 속한 회사의 모든 직원 (요청/승인/거절 포함) 조회
  @Transactional(readOnly = true)
  public List<JoinStatusResponse> getMemberJoinStatus(Long ownerId) {

    Member owner = adminRepository.findById(ownerId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "로그인한 회원 정보를 찾을 수 없습니다."));

    Long companyId = owner.getCompanyId();

    List<Member> members = adminRepository.findByCompanyIdAndIdNotOrderByJoinRequestDateAsc(companyId, ownerId);

    return members.stream()
      .map(JoinStatusResponse::from)
      .toList();
  }

  // 직원 가입 상태 변경 (PENDING / APPROVED / REJECTED 간 전환)
  @Transactional
  public List<JoinStatusResponse> changeMemberRequestStatus (
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
    List<Member> members = adminRepository.findByIdAndCompanyId(targetIds, companyId);

    if (members.size() != targetIds.size()) {
      throw new BaseException(
        GlobalErrorCode.BAD_REQUEST,
        "요청한 직원 중 해당 회사에 속하지 않는 직원이 포함되어 있습니다."
      );
    }

    // id -> Member 매핑
    Map<Long, Member> memberMap = members.stream()
      .collect(Collectors.toMap(Member::getId, m -> m));

    // 각각 상태 변경 + 응답 DTO 생성
    List<JoinStatusResponse> responses = new ArrayList<>();

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

      responses.add(JoinStatusResponse.from(member));
    }



    return responses;
  }
}
