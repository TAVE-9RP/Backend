package com.nexerp.domain.logistics.service;

import com.nexerp.domain.item.model.entity.Item;
import com.nexerp.domain.item.repository.ItemRepository;
import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.domain.logistics.model.request.LogisticsItemTargetQuantityRequest.ItemTargetQuantityDetail;
import com.nexerp.domain.logistics.model.request.LogisticsItemsUpdateRequest.UpdateLogisticsItemDetail;
import com.nexerp.domain.logistics.model.request.LogisticsUpdateRequest;
import com.nexerp.domain.logistics.model.response.LogisticsDetailsResponse;
import com.nexerp.domain.logistics.model.response.LogisticsItemResponse;
import com.nexerp.domain.logistics.model.response.LogisticsSearchResponse;
import com.nexerp.domain.logistics.repository.LogisticsRepository;
import com.nexerp.domain.logisticsItem.model.entity.LogisticsItem;
import com.nexerp.domain.logisticsItem.model.enums.LogisticsProcessingStatus;
import com.nexerp.domain.logisticsItem.repository.LogisticsItemRepository;
import com.nexerp.domain.member.service.MemberService;
import com.nexerp.domain.project.model.entity.Project;
import com.nexerp.domain.project.service.ProjectService;
import com.nexerp.domain.projectmember.repository.ProjectMemberRepository;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogisticsService {

  private final MemberService memberService;
  private final ProjectService projectService;
  private final LogisticsRepository logisticsRepository;
  private final ProjectMemberRepository projectMemberRepository;
  private final ItemRepository itemRepository;
  private final LogisticsItemRepository logisticsItemRepository;

  @Transactional(readOnly = true)
  public List<LogisticsSearchResponse> getCompanyLogisticsSummaries(Long memberId) {

    Long companyId = memberService.getCompanyIdByMemberId(memberId);

    List<Project> projects = projectService.getProjectsWithLogisticsByCompanyId(companyId);

    return LogisticsSearchResponse.fromList(projects);
  }

  @Transactional
  public void updateLogisticsDetails(Long memberId, Long logisticsId,
    LogisticsUpdateRequest request) {

    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    validateAssignee(logistics, memberId);

    logistics.update(
      request.getLogisticsTitle(),
      request.getLogisticsCarrier(),
      request.getLogisticsCarrierCompany(),
      request.getLogisticsDescription()
    );

  }

  @Transactional
  public void requestLogisticsApproval(Long memberId, Long logisticsId) {

    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    validateAssignee(logistics, memberId);

    logistics.requestApproval();
  }

  @Transactional
  public void approveLogistics(Long ownerId, Long logisticsId) {
    Logistics logistics = logisticsRepository.findWithProjectCompanyAndItemsById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    validateSameCompany(ownerId, logistics);

    logistics.approve();
  }

  @Transactional
  public void addLogisticsItems(Long memberId, Long logisticsId,
    List<Long> itemRequests) {
    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    validateAssignee(logistics, memberId);

    Set<Long> requestedIds = validateDuplicateItemIds(itemRequests);

    // 요청 중 새로운 요청만 필터
    Map<Long, LogisticsItem> existingByItemId = mapByItemId(logistics);

    // 이미 등록된 itemId는 제외
    List<Long> newItemIds = requestedIds.stream()
      .filter(id -> !existingByItemId.containsKey(id))
      .toList();

    if (newItemIds.isEmpty()) {
      throw new BaseException(GlobalErrorCode.CONFLICT, "추가할 수 있는 새로운 물품이 없습니다.");
    }

    // 새로운 요청에 대한 Item ID만 추출
    Map<Long, Item> itemMap = findItemsAsMapOrThrow(newItemIds);

    List<LogisticsItem> logisticsItems = newItemIds.stream()
      .map(itemId -> LogisticsItem.create(logistics, itemMap.get(itemId)))
      .toList();

    logisticsItemRepository.saveAll(logisticsItems);
  }

  @Transactional(readOnly = true)
  public List<LogisticsItemResponse> getLogisticsItems(Long memberId, Long logisticsId) {
    Logistics logistics = logisticsRepository.findWithAllDetailsById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    validateSameCompany(memberId, logistics);

    return LogisticsItemResponse.fromList(logistics.getLogisticsItems());

  }

  @Transactional
  public void updateLogisticsItemProgress(Long memberId, Long logisticsId,
    List<UpdateLogisticsItemDetail> itemRequests) {

    Logistics logistics = logisticsRepository.findWithAllDetailsById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    validateAssignee(logistics, memberId);

    List<Long> requestedIdsList = itemRequests.stream()
      .map(UpdateLogisticsItemDetail::getItemId)
      .toList();

    Set<Long> requestedIds = validateDuplicateItemIds(requestedIdsList);

    Map<Long, LogisticsItem> existingByItemId = mapByItemId(logistics);

    validateAllItemsExistInLogistics(requestedIds, existingByItemId);

    // 관계가 없던 itemId 추출
    for (UpdateLogisticsItemDetail detail : itemRequests) {
      LogisticsItem li = existingByItemId.get(detail.getItemId());

      // 출하 수량 변경 + 총 금액 설정
      li.increaseProcessedQuantity(detail.getProcessedQuantity());

      // 목표 출하 기준 상태와 출하일 변경
      if (li.getTargetedQuantity() <= li.getProcessedQuantity()) {
        li.completedLogisticsItem();
      } else if (li.getProcessingStatus() == LogisticsProcessingStatus.NOT_STARTED) {
        li.changeStatus(LogisticsProcessingStatus.IN_PROGRESS);
      }
    }
  }

  @Transactional(readOnly = true)
  public LogisticsDetailsResponse getLogisticsDetails(Long memberId, Long logisticsId) {

    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    validateSameCompany(memberId, logistics);

    return LogisticsDetailsResponse.from(logistics);
  }

  @Transactional
  public void completeLogistics(Long memberId, Long logisticsId) {

    Logistics logistics = logisticsRepository.findWithProjectCompanyAndItemsById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    validateAssignee(logistics, memberId);

    logistics.complete();
  }

  @Transactional
  public void updateTargetQuantities(Long memberId, Long logisticsId,
    List<ItemTargetQuantityDetail> request) {

    Logistics logistics = logisticsRepository.findWithAllDetailsById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    validateAssignee(logistics, memberId);

    List<Long> requestedIds = request.stream()
      .map(ItemTargetQuantityDetail::getItemId)
      .toList();

    Set<Long> uniqueRequestedIds = validateDuplicateItemIds(requestedIds);

    Map<Long, LogisticsItem> existingByItemId = mapByItemId(logistics);

    validateAllItemsExistInLogistics(uniqueRequestedIds, existingByItemId);

    // 수량 업데이트 반영
    for (ItemTargetQuantityDetail detail : request) {
      LogisticsItem item = existingByItemId.get(detail.getItemId());
      item.applyTargetQuantity(detail.getTargetQuantity());
    }
  }

  // 업무와 회원 id 회사 비교
  private void validateSameCompany(Long memberId, Logistics logistics) {

    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);

    if (!memberCompanyId.equals(logistics.getProject().getCompany().getId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "다른 회사의 출하 업무에는 접근할 수 없습니다.");
    }
  }

  // set을 통한 중복 요청 제거
  private Set<Long> validateDuplicateItemIds(List<Long> itemIds) {
    Set<Long> uniqueIds = new HashSet<>(itemIds);
    if (uniqueIds.size() != itemIds.size()) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "요청에 중복된 itemId가 존재합니다.");
    }
    return uniqueIds;
  }

  // 출하 관련 물품 조회
  private Map<Long, LogisticsItem> mapByItemId(Logistics logistics) {
    return logistics.getLogisticsItems().stream()
      .collect(Collectors.toMap(li -> li.getItem().getId(), Function.identity()));
  }

  // 요청과 연관 물품 비교
  private void validateAllItemsExistInLogistics(Set<Long> requestedItemIds,
    Map<Long, LogisticsItem> existingByItemId) {
    List<Long> notExists = requestedItemIds.stream()
      .filter(id -> !existingByItemId.containsKey(id))
      .toList();

    if (!notExists.isEmpty()) {
      throw new BaseException(GlobalErrorCode.STATE_CONFLICT,
        "출하 업무에 등록되지 않은 물품입니다. itemIds=" + notExists);
    }
  }

  // 올바른 물품 요청인지 검증
  private Map<Long, Item> findItemsAsMapOrThrow(List<Long> itemIds) {
    List<Item> found = itemRepository.findAllById(itemIds);
    Map<Long, Item> map = found.stream()
      .collect(Collectors.toMap(Item::getId, Function.identity()));

    List<Long> missing = itemIds.stream()
      .distinct()
      .filter(id -> !map.containsKey(id))
      .toList();

    if (!missing.isEmpty()) {
      throw new BaseException(GlobalErrorCode.NOT_FOUND, "물품을 찾을 수 없습니다. itemIds=" + missing);
    }

    return map;
  }

  private void validateAssignee(Logistics logistics, Long memberId) {

    Long projectId = logistics.getProject().getId();

    boolean isAssigned = projectMemberRepository
      .existsByProjectIdAndMemberId(projectId, memberId);

    if (!isAssigned) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "해당 업무에 접근할 수 없습니다.");
    }
  }

}
