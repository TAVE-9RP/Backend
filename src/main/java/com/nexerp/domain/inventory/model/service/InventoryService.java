package com.nexerp.domain.inventory.model.service;

import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.domain.inventory.model.enums.InventoryStatus;
import com.nexerp.domain.inventory.model.repository.InventoryRepository;
import com.nexerp.domain.inventory.model.request.InventoryCommonUpdateRequest;
import com.nexerp.domain.projectmember.repository.ProjectMemberRepository;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

  private final InventoryRepository inventoryRepository;
  private final ProjectMemberRepository projectMemberRepository;

  public void updateInventoryCommonInfo(
    Long inventoryId,
    Long memberId,
    InventoryCommonUpdateRequest request
  ) {

    validateAssignee(inventoryId, memberId);

    Inventory inv = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    // 상태 검증: 승인 요청(PENDING) 이후에는 수정 불가
    if (inv.getStatus() != InventoryStatus.ASSIGNED) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "ASSIGNED 상태에서만 수정할 수 있습니다.");
    }

    inv.updateCommonInfo(
      request.getTitle(),
      request.getDescription(),
      LocalDateTime.now()
    );
  }

  // 담당자 검증
  private void validateAssignee(Long inventoryId, Long memberId) {
    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업물르 찾을 수 없습니다."));

    Long projectId = inventory.getProject().getId();

    boolean isAssigned = projectMemberRepository
      .existsByProjectIdAndMemberId(projectId, memberId);

    if (!isAssigned) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "해당 업무에 접근할 수 없습니다.");
    }
  }
}
