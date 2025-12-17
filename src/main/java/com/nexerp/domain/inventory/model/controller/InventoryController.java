package com.nexerp.domain.inventory.model.controller;

import com.nexerp.domain.inventory.model.request.InventoryCommonUpdateRequest;
import com.nexerp.domain.inventory.model.service.InventoryService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
@Tag(name = "입고 업무 관련 API", description = "입고 조회, 생성, 물품 추가 등 입고와 관련된 모든 업무")
public class InventoryController {

  private final InventoryService inventoryService;

  @PreAuthorize("hasPermission('INVENTORY', 'WRITE')")
  @PatchMapping("/{inventoryId}")
  @Operation(
    summary = "입고 공통 정보 저장 및 수정 API",
    description = "**물품 추가를 제외한 입고 업무의 공통 정보를 저장 또는 수정합니다.**"
      + "해당 프로젝트에 할당된 담당자만 가능"
      + "입고 업무명(title), 업무 설명(description)을 필수로 지정합니다."
      + "이 API는 URL 경로의 {inventoryId} 값을 통해 수정할 입고 업무를 지정해야 합니다."
      + "프로젝트 넘버는 오너가 프로젝트 생성 시 할당되었기에 별도로 지정하지 않습니다."
      + "수정과 같은 기능을 하기 때문에 수정 시에도 본 API를 활용합니다."
      + "수정 가능 시점은 입고 승인 요청을 보내기 전까지입니다.",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "추가 파라미터 정보",
      required = true,
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = InventoryCommonUpdateRequest.class),
        examples = @ExampleObject(
          name = "입고 공통 정보 저장 예시",
          value = """
          {
           "title": "삼성 물산 과일 입고 건",
           "description": "삼성 물산 망고 200개, 사과 100개, 딸기 100개를 모두 입고 처리하고자 합니다."
          }
          """
        )
      )
    ))
  public BaseResponse<Void> updateInventoryCommon (
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @Parameter(
      name = "inventoryId",
      description = "수정할 입고 업무의 ID (URL Path에 포함)",
      required = true,
      example = "12"
    )
    @PathVariable Long inventoryId,
    @Valid @RequestBody InventoryCommonUpdateRequest request
    ) {
    Long memberId = userDetails.getMemberId();

    inventoryService.updateInventoryCommonInfo(inventoryId, memberId, request);

    return BaseResponse.success();
  }
}
