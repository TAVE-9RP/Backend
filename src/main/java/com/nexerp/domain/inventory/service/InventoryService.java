package com.nexerp.domain.inventory.service;

import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.domain.inventory.model.enums.InventoryStatus;
import com.nexerp.domain.inventory.model.request.InventoryItemAddRequest;
import com.nexerp.domain.inventory.model.request.InventoryTargetQuantityUpdateRequest;
import com.nexerp.domain.inventory.model.response.InventoryItemAddResponse;
import com.nexerp.domain.inventory.model.response.InventoryItemResponse;
import com.nexerp.domain.inventory.repository.InventoryRepository;
import com.nexerp.domain.inventory.model.request.InventoryCommonUpdateRequest;
import com.nexerp.domain.inventoryitem.model.entity.InventoryItem;
import com.nexerp.domain.inventoryitem.model.enums.InventoryProcessingStatus;
import com.nexerp.domain.inventoryitem.repository.InventoryItemRepository;
import com.nexerp.domain.item.model.entity.Item;
import com.nexerp.domain.item.repository.ItemRepository;
import com.nexerp.domain.projectmember.repository.ProjectMemberRepository;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

  private final InventoryRepository inventoryRepository;
  private final ProjectMemberRepository projectMemberRepository;
  private final ItemRepository itemRepository;
  private final InventoryItemRepository inventoryItemRepository;

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

  @Transactional
  public InventoryItemAddResponse addInventoryItems (
    Long memberId,
    Long inventoryId,
    InventoryItemAddRequest request
  ) {

    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    validateAssignee(inventoryId, memberId);

    List<Long> createdIds = new ArrayList<>();

    for (Long itemId : request.getItemIds()) {

      if(inventoryItemRepository.existsByInventoryIdAndItemId(inventoryId, itemId)) {
        continue; // 이미 존재하는 물품이면 스킵
      }

      Item item = itemRepository.findById(itemId)
        .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "해당 품목을 찾을 수 없습니다."));

      InventoryItem inventoryItem = InventoryItem.builder()
        .inventory(inventory)
        .item(item)
        .quantity(0L) // 목표 수량은 별도로 입력
        .processed_quantity(0L)
        .status(InventoryProcessingStatus.NOT_STARTED)
        .build();

      InventoryItem saved = inventoryItemRepository.save(inventoryItem);
      createdIds.add(saved.getId());
    }

    return InventoryItemAddResponse.from(createdIds);
  }

  @Transactional
  public void updateTargetQuantities(
    Long memberId,
    Long inventoryId,
    InventoryTargetQuantityUpdateRequest request
  ) {
    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    validateAssignee(inventoryId, memberId);

    if (inventory.getStatus() != InventoryStatus.ASSIGNED) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "승인 요청 전까지만 설정 가능합니다.");
    }

    for (InventoryTargetQuantityUpdateRequest.QuantityUpdateUnit unit : request.getUpdates()) {
      InventoryItem inventoryItem = inventoryItemRepository.findById(unit.getInventoryItemId())
        .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 예정 품목을 찾을 수 없습니다."));

      if (!inventoryItem.getInventory().getId().equals(inventoryId)) {
        throw new BaseException(GlobalErrorCode.FORBIDDEN, "잘못된 입고 품목입니다.");
      }

      inventoryItem.updateTargetQuantity(unit.getTargetQuantity());
    }
  }

  @Transactional(readOnly = true)
  public List<InventoryItemResponse> getInventoryItems(Long inventoryId) {

    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));


    return inventoryItemRepository.findAllByInventoryId(inventoryId)
      .stream()
      .map(InventoryItemResponse::from)
      .toList();
  }

  // 담당자 검증
  private void validateAssignee(Long inventoryId, Long memberId) {
    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    Long projectId = inventory.getProject().getId();

    boolean isAssigned = projectMemberRepository
      .existsByProjectIdAndMemberId(projectId, memberId);

    if (!isAssigned) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "해당 업무에 접근할 수 없습니다.");
    }
  }
}
