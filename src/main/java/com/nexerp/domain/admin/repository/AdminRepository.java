package com.nexerp.domain.admin.repository;

import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Member, Long> {
  // 같은 회사 직원 전체 조회 (가입 요청 화면) 오너는 제외하도록
  List<Member> findByCompanyIdAndIdNotOrderByJoinRequestDateAsc(Long companyId, Long excludeMemberId);

  // 같은 회사 소속의 특정 직원 검증 (권한 체크까지 포함)
  List<Member> findByIdInAndCompanyId(List<Long> ids, Long companyId);

  // 같은 회사 승인된 소속 직원만 조회 (권한 관리, 프로젝트 할당 조회 시 사용)
  List<Member> findByCompanyIdAndRequestStatusAndPositionNot(
    Long companyId,
    MemberRequestStatus status,
    MemberPosition excludedPosition
  );
}
