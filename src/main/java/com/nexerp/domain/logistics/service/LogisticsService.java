package com.nexerp.domain.logistics.service;

import com.nexerp.domain.item.model.entity.Item;
import com.nexerp.domain.item.repository.ItemRepository;
import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.domain.logistics.model.request.LogisticsItemTargetQuantityRequest;
import com.nexerp.domain.logistics.model.request.LogisticsItemTargetQuantityRequest.ItemTargetQuantityDetail;
import com.nexerp.domain.logistics.model.request.LogisticsItemsCreateRequest.CreateLogisticsItemDetail;
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
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import java.util.ArrayList;
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
  private final ItemRepository itemRepository;
  private final LogisticsItemRepository logisticsItemRepository;

  @Transactional(readOnly = true)
  public List<LogisticsSearchResponse> searchLogisticsByMemberId(Long memberId) {
    //멈버 회사 추출
    Long companyId = memberService.getCompanyIdByMemberId(memberId);

    //프로젝트 조회
    List<Project> projects = projectService.getProjectsWithLogisticsByCompanyId(companyId);

    List<LogisticsSearchResponse> responseList = projects.stream()
      .filter(p -> p.getLogistics() != null)
      .map(project -> {
        Logistics logistics = project.getLogistics();

        List<String> memberNames = project.getProjectMembers().stream()
          .map(pm -> pm.getMember().getName())
          .toList();

        return LogisticsSearchResponse.builder()
          .logisticsId(logistics.getId())
          .logisticsTitle(logistics.getTitle())
          .customer(project.getCustomer())
          .requestedAt(logistics.getRequestedAt()) // LocalDate -> LocalDateTime 변환
          .projectMembers(memberNames)
          .build();
      })
      .toList();

    return responseList;
  }

  // Logistics + Project + Company
  @Transactional
  public void updateLogisticsInfo(Long memberId, Long logisticsId,
    LogisticsUpdateRequest request) {

    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    // 회사 검증
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);
    if (!memberCompanyId.equals(logistics.getProject().getCompany().getId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "다른 회사의 출하 업무에는 접근할 수 없습니다.");
    }

    logistics.update(
      request.getLogisticsTitle(),
      request.getLogisticsCarrier(),
      request.getLogisticsCarrierCompany(),
      request.getLogisticsDescription()
    );

  }

  // Logistics + Project + Company
  @Transactional
  public void requestLogisticsApproval(Long memberId, Long logisticsId) {

    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    // 회사 검증
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);
    if (!memberCompanyId.equals(logistics.getProject().getCompany().getId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "다른 회사의 출하 업무에는 접근할 수 없습니다.");
    }

    logistics.requestApproval();
  }

  // Logistics + Project + Company + LogisticsItems
  @Transactional
  public void approveLogistics(Long ownerId, Long logisticsId) {
    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    // 회사 검증
    Long memberCompanyId = memberService.getCompanyIdByMemberId(ownerId);
    if (!memberCompanyId.equals(logistics.getProject().getCompany().getId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "다른 회사의 출하 업무에는 접근할 수 없습니다.");
    }

    logistics.approve();
  }

  //Logistics + Project + Company + LogisticsItems + Item
  @Transactional
  public void addItems(Long memberId, Long logisticsId,
    List<CreateLogisticsItemDetail> itemRequests) {
    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    // 회사 검증
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);
    if (!memberCompanyId.equals(logistics.getProject().getCompany().getId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "다른 회사의 출하 업무에는 접근할 수 없습니다.");
    }

    // 이미 등록된 물품 ID 추출
    Set<Long> existingItemIds = logistics.getLogisticsItems().stream()
      .map(li -> li.getItem().getId())
      .collect(Collectors.toSet());

    // 요청 중 새로운 요청만 필터
    List<CreateLogisticsItemDetail> newRequests = itemRequests.stream()
      .filter(request -> !existingItemIds.contains(request.getItemId()))
      .toList();

    if (newRequests.isEmpty()) {
      throw new BaseException(GlobalErrorCode.CONFLICT, "추가할 수 있는 새로운 물품이 없습니다.");
    }

    // 새로운 요청에 대한 Item ID만 추출
    List<Long> newItemIds = newRequests.stream()
      .map(CreateLogisticsItemDetail::getItemId)
      .toList();

    List<Item> foundItems = itemRepository.findAllById(newItemIds);

    Map<Long, Item> itemMap = foundItems.stream()
      .collect(Collectors.toMap(Item::getId, Function.identity()));

    List<LogisticsItem> logisticsItems = new ArrayList<>();

    for (CreateLogisticsItemDetail detail : newRequests) {
      Item item = itemMap.get(detail.getItemId());

      if (item == null) {
        throw new BaseException(GlobalErrorCode.NOT_FOUND,
          "ID: " + detail.getItemId() + " 물품을 찾을 수 없습니다.");
      }

      LogisticsItem newItem = LogisticsItem.create(logistics, item);
      logisticsItems.add(newItem);

    }

    logisticsItemRepository.saveAll(logisticsItems);

  }

  //Logistics + Project + Company + LogisticsItems + Item
  @Transactional(readOnly = true)
  public List<LogisticsItemResponse> getLogisticsItems(Long memberId, Long logisticsId) {
    Logistics logistics = logisticsRepository.findWithItemsById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    // 회사 검증
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);
    if (!memberCompanyId.equals(logistics.getProject().getCompany().getId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "다른 회사의 출하 업무에는 접근할 수 없습니다.");
    }

    return logistics.getLogisticsItems().stream()
      .map(li -> {
        Item item = li.getItem();
        return LogisticsItemResponse.builder()
          .itemId(item.getId())
          .itemCode(item.getCode())
          .itemName(item.getName())
          .processedQuantity(li.getProcessedQuantity())
          .targetedQuantity(li.getTargetedQuantity())
          .itemPrice(item.getPrice())
          .itemTotalPrice(li.getTotalPrice())
          .unitOfMeasure(item.getUnitOfMeasure())
          .logisticsProcessingStatus(li.getProcessingStatus())
          .build();
      })
      .toList();

  }

  //Logistics + Project + Company + LogisticsItems + Item
  @Transactional
  public void updateLogisticsItems(Long memberId, Long logisticsId,
    List<UpdateLogisticsItemDetail> itemRequests) {

    //진행 중인 물품인지 먼저 확인

    Logistics logistics = logisticsRepository.findWithItemsById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    // 회사 검증
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);
    if (!memberCompanyId.equals(logistics.getProject().getCompany().getId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "다른 회사의 출하 업무에는 접근할 수 없습니다.");
    }

    // 요청 itemId 추출
    List<Long> requestedIdsList = itemRequests.stream()
      .map(UpdateLogisticsItemDetail::getItemId)
      .toList();

    // 요청 itemId 중복 검증
    Set<Long> requestedIds = new HashSet<>(requestedIdsList);
    if (requestedIds.size() != requestedIdsList.size()) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "요청에 중복된 itemId가 존재합니다.");
    }

    // 기존 logistics의 LogisticsItem을 itemId 기준으로 맵핑
    Map<Long, LogisticsItem> existingByItemId = logistics.getLogisticsItems().stream()
      .collect(Collectors.toMap(li -> li.getItem().getId(), Function.identity()));

    // 관계가 없던 itemId 추출
    List<Long> notExists = requestedIds.stream()
      .filter(id -> !existingByItemId.containsKey(id))
      .toList();

    if (!notExists.isEmpty()) {
      throw new BaseException(GlobalErrorCode.STATE_CONFLICT,
        "출하 업무에 등록되지 않은 물품입니다. itemIds=" + notExists);
    }

    for (UpdateLogisticsItemDetail detail : itemRequests) {
      LogisticsItem li = existingByItemId.get(detail.getItemId());

      li.changeProcessedQuantity(detail.getProcessedQuantity());

      // 목표 출하 기준 상태 변경
      if (li.getTargetedQuantity() <= detail.getProcessedQuantity()) {
        li.changeStatus(LogisticsProcessingStatus.COMPLETED);
      } else if (li.getProcessingStatus() == LogisticsProcessingStatus.COMPLETED) {
        li.changeStatus(LogisticsProcessingStatus.IN_PROGRESS);
      }
    }
  }

  // Logistics + Project + Company
  @Transactional(readOnly = true)
  public LogisticsDetailsResponse getLogisticsDetails(Long memberId, Long logisticsId) {
    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    // 회사 검증
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);
    if (!memberCompanyId.equals(logistics.getProject().getCompany().getId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "다른 회사의 출하 업무에는 접근할 수 없습니다.");
    }

    return LogisticsDetailsResponse.builder()
      .projectNumber(logistics.getProject().getNumber())
      .logisticsTitle(logistics.getTitle())
      .logisticsDescription(logistics.getDescription())
      .logisticsCarrier(logistics.getCarrier())
      .logisticsCarrierCompany(logistics.getCarrierCompany())
      .logisticsRequestedAt(logistics.getRequestedAt())
      .localCompletedAt(logistics.getCompletedAt())
      .logisticsSatus(logistics.getStatus())
      .build();
  }

  // Logistics + Project + Company + LogisticsItems
  @Transactional
  public void completeLogisticsStatus(Long memberId, Long logisticsId) {
    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    // 회사 검증
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);
    if (!memberCompanyId.equals(logistics.getProject().getCompany().getId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "다른 회사의 출하 업무에는 접근할 수 없습니다.");
    }

    logistics.complete();
  }

  //Logistics + Project + Company + LogisticsItems + Item
  @Transactional
  public void updateLogisticsItemTargetQuantity(Long memberId, Long logisticsId,
    List<ItemTargetQuantityDetail> request) {
    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    // 회사 검증
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);
    if (!memberCompanyId.equals(logistics.getProject().getCompany().getId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "다른 회사의 출하 업무에는 접근할 수 없습니다.");
    }

    Map<Long, Long> targetQtyByItemId = request.stream()
      .collect(Collectors.toMap(
        LogisticsItemTargetQuantityRequest.ItemTargetQuantityDetail::getItemId,
        LogisticsItemTargetQuantityRequest.ItemTargetQuantityDetail::getTargetQuantity,
        (a, b) -> { // 같은 itemId가 중복으로 들어오면 예외
          throw new BaseException(GlobalErrorCode.STATE_CONFLICT, "요청에 중복된 itemId가 존재합니다.");
        }
      ));

    // 실제 logistics에 포함된 itemId set
    Set<Long> logisticsItemIds = logistics.getLogisticsItems().stream()
      .map(li -> li.getItem().getId())
      .collect(Collectors.toSet());

    // 요청 itemId가 해당 출하업무에 포함된 물품인지 검증
    for (Long itemId : targetQtyByItemId.keySet()) {
      if (!logisticsItemIds.contains(itemId)) {
        throw new BaseException(GlobalErrorCode.NOT_FOUND,
          "출하 업무에 포함되지 않은 물품입니다. itemId=" + itemId);
      }
    }

    // targetedQuantity, totalPrice 반영 (미포함은 기존 유지)
    for (LogisticsItem logisticsItem : logistics.getLogisticsItems()) {
      Long itemId = logisticsItem.getItem().getId();
      Long targetQty = targetQtyByItemId.get(itemId);
      if (targetQty != null) {
        logisticsItem.applyTargetQuantity(targetQty);
      }
    }

  }
}
