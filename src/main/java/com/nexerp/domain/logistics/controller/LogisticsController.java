package com.nexerp.domain.logistics.controller;

import com.nexerp.domain.logistics.model.request.LogisticsItemTargetQuantityRequest;
import com.nexerp.domain.logistics.model.request.LogisticsItemsCreateRequest;
import com.nexerp.domain.logistics.model.request.LogisticsItemsUpdateRequest;
import com.nexerp.domain.logistics.model.request.LogisticsUpdateRequest;
import com.nexerp.domain.logistics.model.response.LogisticsDetailsResponse;
import com.nexerp.domain.logistics.model.response.LogisticsItemResponse;
import com.nexerp.domain.logistics.model.response.LogisticsSearchResponse;
import com.nexerp.domain.logistics.service.LogisticsService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.details.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
public class LogisticsController {

  private final LogisticsService logisticsService;

  // 출하 업무 전체 조회
  @GetMapping
  @PreAuthorize("hasPermission('LOGISTICS', 'READ')")
  public BaseResponse<List<LogisticsSearchResponse>> getCompanyLogisticsSummaries(
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getMemberId();
    List<LogisticsSearchResponse> result = logisticsService.getCompanyLogisticsSummaries(memberId);

    return BaseResponse.success(result);
  }

  // 출하 업무 정보 수정
  @PatchMapping("/{logisticsId}")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  public BaseResponse<Void> updateLogisticsDetails(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId,
    @Valid @RequestBody LogisticsUpdateRequest logisticsUpdateRequest
  ) {
    Long memberId = userDetails.getMemberId();

    logisticsService.updateLogisticsDetails(memberId, logisticsId, logisticsUpdateRequest);

    return BaseResponse.success();
  }

  // 출하 업무 승인 요청
  @PatchMapping("/{logisticsId}/status")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  public BaseResponse<Void> requestLogisticsApproval(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId
  ) {
    Long memberId = userDetails.getMemberId();
    logisticsService.requestLogisticsApproval(memberId, logisticsId);
    return BaseResponse.success();
  }

  // 출하 물품 추가
  @PostMapping("/{logisticsId}/items")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  public BaseResponse<Void> createLogisticsItem(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId,
    @Valid @RequestBody LogisticsItemsCreateRequest request
  ) {
    Long memberId = userDetails.getMemberId();
    logisticsService.addLogisticsItems(memberId, logisticsId, request.getItemId());
    return BaseResponse.success();
  }

  // 출하 물품 조회
  @GetMapping("/{logisticsId}/items")
  @PreAuthorize("hasPermission('LOGISTICS', 'READ')")
  public BaseResponse<List<LogisticsItemResponse>> getLogisticsItems(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId
  ) {
    Long memberId = userDetails.getMemberId();
    List<LogisticsItemResponse> responses = logisticsService.getLogisticsItems(memberId,
      logisticsId);
    return BaseResponse.success(responses);
  }

  // 출하
  @PatchMapping("/{logisticsId}/items")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  public BaseResponse<Void> updateLogisticsItemProgress(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId,
    @Valid @RequestBody LogisticsItemsUpdateRequest request
  ) {
    Long memberId = userDetails.getMemberId();
    logisticsService.updateLogisticsItemProgress(memberId, logisticsId, request.getItems());
    return BaseResponse.success();
  }

  // 출하 업무 상세 보기
  @GetMapping("/{logisticsId}")
  @PreAuthorize("hasPermission('LOGISTICS', 'READ')")
  public BaseResponse<LogisticsDetailsResponse> getLogisticsDetails(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId
  ) {
    Long memberId = userDetails.getMemberId();
    LogisticsDetailsResponse response = logisticsService.getLogisticsDetails(memberId, logisticsId);
    return BaseResponse.success(response);
  }

  //업무 완료 처리
  @PatchMapping("/{logisticsId}/status/complete")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  public BaseResponse<Void> completeLogistics(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId
  ) {
    Long memberId = userDetails.getMemberId();
    logisticsService.completeLogistics(memberId, logisticsId);
    return BaseResponse.success();
  }

  // 목표 출하 수량 + 총 판매액 설정
  @PatchMapping("/{logisticsId}/items/quantities")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  public BaseResponse<Void> updateTargetQuantities(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId,
    @Valid @RequestBody LogisticsItemTargetQuantityRequest request
  ) {
    Long memberId = userDetails.getMemberId();
    logisticsService.updateTargetQuantities(memberId, logisticsId, request.getItems());
    return BaseResponse.success();
  }

}
