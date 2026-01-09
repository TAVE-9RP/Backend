package com.nexerp.domain.inventory.service;

import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.domain.inventory.model.request.InventoryCommonUpdateRequest;
import com.nexerp.domain.inventory.model.request.InventoryItemAddRequest;
import com.nexerp.domain.inventory.model.request.InventoryProcessRequest;
import com.nexerp.domain.inventory.model.request.InventoryTargetQuantityUpdateRequest;
import com.nexerp.domain.inventory.model.response.InventoryDetailResponse;
import com.nexerp.domain.inventory.model.response.InventoryItemAddResponse;
import com.nexerp.domain.inventory.model.response.InventoryItemResponse;
import com.nexerp.domain.inventory.model.response.InventorySummaryResponse;
import com.nexerp.domain.inventory.repository.InventoryRepository;
import com.nexerp.domain.inventoryitem.model.entity.InventoryItem;
import com.nexerp.domain.inventoryitem.repository.InventoryItemRepository;
import com.nexerp.domain.item.model.entity.Item;
import com.nexerp.domain.item.model.entity.ItemHistory;
import com.nexerp.domain.item.repository.ItemHistoryRepository;
import com.nexerp.domain.item.repository.ItemRepository;
import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.service.MemberService;
import com.nexerp.domain.project.service.ProjectService;
import com.nexerp.domain.projectmember.model.entity.ProjectMember;
import com.nexerp.domain.projectmember.repository.ProjectMemberRepository;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.common.model.TaskProcessingStatus;
import com.nexerp.global.common.model.TaskStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

  private final InventoryRepository inventoryRepository;
  private final ProjectMemberRepository projectMemberRepository;
  private final ItemRepository itemRepository;
  private final InventoryItemRepository inventoryItemRepository;
  private final ProjectService projectService;
  private final MemberService memberService;
  private final ItemHistoryRepository itemHistoryRepository;

  @Transactional
  public void updateInventoryCommonInfo(
    Long inventoryId,
    Long memberId,
    InventoryCommonUpdateRequest request
  ) {

    validateAssignee(inventoryId, memberId);

    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    // 상태 검증: 승인 요청(PENDING) 이후에는 수정 불가
    if (inventory.getStatus() != TaskStatus.ASSIGNED
      && inventory.getStatus() != TaskStatus.REJECT) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "ASSIGNED 또는 REJECT 상태에서만 수정할 수 있습니다.");
    }

    inventory.updateCommonInfo(
      request.getInventoryTitle(),
      request.getInventoryDescription()
    );
  }

  @Transactional
  public InventoryItemAddResponse addInventoryItems(
    Long memberId,
    Long inventoryId,
    InventoryItemAddRequest request
  ) {

    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    if (inventory.getStatus() != TaskStatus.ASSIGNED
      && inventory.getStatus() != TaskStatus.REJECT) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "ASSIGNED 또는 REJECT 상태에서만 추가 할 수 있습니다.");
    }

    validateAssignee(inventoryId, memberId);

    List<Long> createdIds = new ArrayList<>();

    for (Long itemId : request.getItemIds()) {

      if (inventoryItemRepository.existsByInventoryIdAndItemId(inventoryId, itemId)) {
        continue; // 이미 존재하는 물품이면 스킵
      }

      Item item = itemRepository.findById(itemId)
        .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "해당 품목을 찾을 수 없습니다."));

      InventoryItem inventoryItem = InventoryItem.builder()
        .inventory(inventory)
        .item(item)
        .quantity(0L) // 목표 수량은 별도로 입력
        .processed_quantity(0L)
        .status(TaskProcessingStatus.NOT_STARTED)
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

    if (inventory.getStatus() != TaskStatus.ASSIGNED
      && inventory.getStatus() != TaskStatus.REJECT) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "ASSIGNED 또는 REJECT 상태에서만 수정할 수 있습니다.");
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

  @Transactional
  public void requestApproval(Long memberId, Long inventoryId) {

    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    validateAssignee(inventoryId, memberId);

    if (inventory.getStatus() != TaskStatus.ASSIGNED
      && inventory.getStatus() != TaskStatus.REJECT) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "ASSIGNED 또는 REJECT 상태에서만 수정할 수 있습니다.");
    }

    boolean hasItem = inventoryItemRepository.existsByInventoryId(inventoryId);
    if (!hasItem) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "입고 예정 품목 1개 이상 필요합니다.");
    }

    inventory.updateStatus(TaskStatus.PENDING, LocalDateTime.now());
  }

  @Transactional
  public void processReceiving(Long memberId, Long inventoryId,
    InventoryProcessRequest request) {
    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    validateAssignee(inventoryId, memberId);

    if (inventory.getStatus() != TaskStatus.IN_PROGRESS) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "IN_PROGRESS 상태에서만 입고 처리할 수 있습니다.");
    }

    Member member = memberService.getMemberByMemberId(memberId);

    for (InventoryProcessRequest.ProcessUnit unit : request.getItems()) {

      InventoryItem inventoryItem = inventoryItemRepository.findById(unit.getInventoryItemId())
        .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 예정 품목을 찾을 수 없습니다."));

      if (!inventoryItem.getInventory().getId().equals(inventoryId)) {
        throw new BaseException(GlobalErrorCode.FORBIDDEN, "잘못된 입고 품목입니다.");
      }

      Long qty = unit.getReceiveQuantity();

      // 현재까지 입고 반영
      inventoryItem.updateProcessedQuantity(qty);

      // 품목 재고 증가
      Item item = inventoryItem.getItem();
      item.increaseQuantity(qty);

      // 재고 기록 남기기
      ItemHistory itemHistory = ItemHistory.received(
        item, inventoryItem.getId(), member, qty, item.getQuantity()
      );
      itemHistoryRepository.save(itemHistory);

      // 상태 갱신
      if (inventoryItem.getProcessed_quantity() >= inventoryItem.getQuantity()) {
        inventoryItem.updateStatus(TaskProcessingStatus.COMPLETED);
      } else {
        inventoryItem.updateStatus(TaskProcessingStatus.IN_PROGRESS);
      }
    }
  }

  @Transactional
  public void completeInventory(Long memberId, Long inventoryId) {

    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    validateAssignee(inventoryId, memberId);

    // 진행 중(IN_PROGRESS) 상태에서만 완료 가능
    if (inventory.getStatus() != TaskStatus.IN_PROGRESS) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST,
        "진행 중(IN_PROGRESS) 상태에서만 업무를 완료할 수 있습니다.");
    }

    // 모든 품목이 완료 상태인지 확인
    boolean allDone = inventoryItemRepository
      .findAllByInventoryId(inventoryId)
      .stream()
      .allMatch(item -> item.getStatus() == TaskProcessingStatus.COMPLETED);

    if (!allDone) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "아직 완료되지 않은 품목이 있습니다.");
    }

    // 업무 최종 완료
    inventory.updateStatus(TaskStatus.COMPLETED, LocalDateTime.now());
    projectService.completeProject(inventory.getProject().getId());
  }

  public List<InventorySummaryResponse> getInventoryList(Long memberId) {

    Member member = memberService.getMemberByMemberId(memberId);

    Long companyId = member.getCompanyId();

    List<Inventory> inventories = inventoryRepository.findAllByProject_Company_Id(companyId);

    return inventories.stream()
      .map(inv -> {

        // 품목 요약 (애플망고 외 2개)
        List<InventoryItem> items = inventoryItemRepository.findAllByInventoryId(inv.getId());
        String itemSummary;
        if (items.isEmpty()) {
          itemSummary = "-";
        } else if (items.size() == 1) {
          itemSummary = items.get(0).getItem().getName();
        } else {
          itemSummary = items.get(0).getItem().getName()
            + " 외 " + (items.size() - 1) + "개";
        }

        // 담당자 요약 (홍길동 외 3명)
        List<ProjectMember> projectMembers = projectMemberRepository.findAllByProjectId(
          inv.getProject().getId());
        List<Member> members = projectMembers.stream()
          .map(ProjectMember::getMember)
          .toList();

        String assigneeSummary;
        if (members.isEmpty()) {
          throw new BaseException(GlobalErrorCode.NOT_FOUND, "프로젝트 담당자를 찾을 수 없습니다.");
        } else if (members.size() == 1) {
          assigneeSummary = members.get(0).getName();
        } else {
          assigneeSummary = members.get(0).getName()
            + " 외 " + (members.size() - 1) + "명";
        }

        return InventorySummaryResponse.from(
          inv, itemSummary, assigneeSummary
        );
      })
      .toList();
  }

  // 업무 상세 조회
  public InventoryDetailResponse getInventoryDetail(Long memberId, Long inventoryId) {

    Inventory inventory = inventoryRepository.findById(inventoryId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "입고 업무를 찾을 수 없습니다."));

    validateCompany(memberId, inventory.getProject().getCompany().getId());

    List<ProjectMember> projectMembers = projectMemberRepository.findAllByProjectId(
      inventory.getProject().getId());

    // 입고 담당자만 필터링
    List<String> assignees = projectMembers.stream()
      .map(ProjectMember::getMember)
      .filter(m -> m.getDepartment() == MemberDepartment.INVENTORY)
      .map(Member::getName)
      .toList();

    return InventoryDetailResponse.from(
      inventory, assignees
    );
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

  // 회사 검증
  private void validateCompany(Long memberId, Long companyId) {
    Member member = memberService.getMemberByMemberId(memberId);

    if (!member.getCompanyId().equals(companyId)) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "회사 정보가 일치하지 않습니다.");
    }
  }

  public List<InventorySummaryResponse> getInventoryAssignees(Long memberId) {

    Member member = memberService.getMemberByMemberId(memberId);

    List<Inventory> inventories = inventoryRepository.findAllAssignedToMember(member.getCompanyId(),
      memberId);
    return inventories.stream()
      .map(inv -> {

        // 품목 요약 (애플망고 외 2개)
        List<InventoryItem> items = inventoryItemRepository.findAllByInventoryId(inv.getId());
        String itemSummary;
        if (items.isEmpty()) {
          itemSummary = "-";
        } else if (items.size() == 1) {
          itemSummary = items.get(0).getItem().getName();
        } else {
          itemSummary = items.get(0).getItem().getName()
            + " 외 " + (items.size() - 1) + "개";
        }

        // 담당자 요약 (홍길동 외 3명)
        List<ProjectMember> projectMembers = projectMemberRepository.findAllByProjectId(
          inv.getProject().getId());
        List<Member> members = projectMembers.stream()
          .map(ProjectMember::getMember)
          .toList();

        String assigneeSummary;
        if (members.isEmpty()) {
          throw new BaseException(GlobalErrorCode.NOT_FOUND, "프로젝트 담당자를 찾을 수 없습니다.");
        } else if (members.size() == 1) {
          assigneeSummary = members.get(0).getName();
        } else {
          assigneeSummary = members.get(0).getName()
            + " 외 " + (members.size() - 1) + "명";
        }

        return InventorySummaryResponse.from(
          inv, itemSummary, assigneeSummary
        );
      })
      .toList();
  }
}
