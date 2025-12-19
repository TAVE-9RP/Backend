package com.nexerp.domain.logistics.controller;

import com.nexerp.domain.logistics.model.request.LogisticsItemsCreateRequest;
import com.nexerp.domain.logistics.model.request.LogisticsUpdateRequest;
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

  @GetMapping
  @PreAuthorize("hasPermission('LOGISTICS', 'READ')")
  public BaseResponse<List<LogisticsSearchResponse>> getLogisticsList(
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getMemberId();
    List<LogisticsSearchResponse> result = logisticsService.searchLogisticsByMemberId(memberId);

    return BaseResponse.success(result);
  }

  @PatchMapping("/{logisticsId}")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  public BaseResponse<Void> updateLogisticsInfo(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId,
    @Valid @RequestBody LogisticsUpdateRequest logisticsUpdateRequest
  ) {
    Long memberId = userDetails.getMemberId();

    logisticsService.updateLogisticsInfo(memberId, logisticsId, logisticsUpdateRequest);

    return BaseResponse.success();
  }

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

  @PostMapping("/{logisticsId}/items")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  public BaseResponse<Void> createLogisticsItem(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId,
    @Valid @RequestBody LogisticsItemsCreateRequest request
  ) {
    Long memberId = userDetails.getMemberId();
    logisticsService.addItems(memberId, logisticsId, request.getItems());
    return BaseResponse.success();
  }

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

}
