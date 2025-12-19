package com.nexerp.domain.logistics.service;

import com.nexerp.domain.item.model.entity.Item;
import com.nexerp.domain.item.repository.ItemRepository;
import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.domain.logistics.model.request.LogisticsItemsCreateRequest.LogisticsItemDetail;
import com.nexerp.domain.logistics.model.request.LogisticsUpdateRequest;
import com.nexerp.domain.logistics.model.response.LogisticsSearchResponse;
import com.nexerp.domain.logistics.repository.LogisticsRepository;
import com.nexerp.domain.logisticsItem.model.entity.LogisticsItem;
import com.nexerp.domain.logisticsItem.repository.LogisticsItemRepository;
import com.nexerp.domain.member.service.MemberService;
import com.nexerp.domain.project.model.entity.Project;
import com.nexerp.domain.project.service.ProjectService;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import java.util.List;
import java.util.Map;
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

  @Transactional
  public void addItems(Long memberId, Long logisticsId, List<LogisticsItemDetail> itemRequests) {
    Logistics logistics = logisticsRepository.findWithProjectAndCompanyById(logisticsId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "출하 업무를 찾을 수 없습니다."));

    // 회사 검증
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);
    if (!memberCompanyId.equals(logistics.getProject().getCompany().getId())) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN, "다른 회사의 출하 업무에는 접근할 수 없습니다.");
    }

    // 요청의 물품 id 추출
    List<Long> itemIds = itemRequests.stream()
      .map(LogisticsItemDetail::getItemId)
      .toList();

    List<Item> foundItems = itemRepository.findAllById(itemIds);

    Map<Long, Item> itemMap = foundItems.stream()
      .collect(Collectors.toMap(Item::getId, Function.identity()));

    List<LogisticsItem> logisticsItems = itemRequests.stream()
      .map(detail -> {
        Item item = itemMap.get(detail.getItemId());

        if (item == null) {
          throw new BaseException(GlobalErrorCode.NOT_FOUND,
            "ID: " + detail.getItemId() + " 물품을 찾을 수 없습니다.");
        }
        return LogisticsItem.create(
          logistics,
          item,
          detail.getLogisticsTargetedQuantity()
        );

      })
      .toList();

    logisticsItemRepository.saveAll(logisticsItems);
  }

}
