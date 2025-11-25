package com.nexerp.domain.admin.service;

import com.nexerp.domain.admin.model.request.JoinStatusUpdateRequest;
import com.nexerp.domain.admin.model.response.JoinStatusResponse;
import com.nexerp.domain.admin.repository.AdminRepository;
import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    List<Member> members = adminRepository.findByCompanyIdOrderByJoinRequestDateAsc(companyId);

    return members.stream()
      .map(JoinStatusResponse::from)
      .toList();
  }

  // 직원 가입 상태 변경 (PENDING / APPROVED / REJECTED 간 전환)
  @Transactional
  public JoinStatusResponse changeMemberRequestStatus (
    Long ownerId,
    Long targetMemberId,
    JoinStatusUpdateRequest request
  ) {
    Member owner = adminRepository.findById(ownerId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "로그인한 회원 정보를 찾을 수 없습니다."));

    if (owner.getPosition() != MemberPosition.OWNER) {
      throw new BaseException(GlobalErrorCode.UNAUTHORIZED, "직원 가입 요청 관리는 회사 오너만 사용할 수 있습니다.");
    }

    Long companyId = owner.getCompanyId();

    Member member = adminRepository
      .findByIdAndCompanyId(targetMemberId, companyId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "해당 회사에 속한 직원을 찾을 수 없습니다."));

    MemberRequestStatus newStatus = request.getNewStatus();
    member.changeRequestStatus(newStatus);

    return JoinStatusResponse.from(member);
  }
}
