package com.nexerp.domain.inventory.controller;

import com.nexerp.domain.inventory.model.request.InventoryCommonUpdateRequest;
import com.nexerp.domain.inventory.model.request.InventoryItemAddRequest;
import com.nexerp.domain.inventory.model.request.InventoryTargetQuantityUpdateRequest;
import com.nexerp.domain.inventory.model.response.InventoryItemAddResponse;
import com.nexerp.domain.inventory.service.InventoryService;
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

  @PostMapping("/{inventoryId}/items/batch")
  @PreAuthorize("hasPermission('INVENTORY', 'WRITE')")
  @Operation(summary = "입고 예정 품목 추가 API",
    description = """
        입고 업무에 여러 개의 품목 또는 한 개의 품목을 추가합니다.
        기존 재고 검색을 통해서 추가할 때는 여러 개를 추가하겠지만,
        새 물품 추가를 통해서 추가 시에는 한 개씩 추가 가능합니다.
        
        **입고 예정 품목(Inventory_Item) 생성 전용 API**  
        품목의 목표 입고 수량은 이 API에서 입력하지 않으며,  
        별도의 '목표 수량 설정 API'에서 진행합니다.  
        이미 목록에 존재하는 품목은 자동으로 제외됩니다.  
        품목은 '승인 요청' 이전 상태(ASSIGNED)에서만 추가가 가능합니다.  

        주의  
        - 실제 재고(Item.quantity) 수량에는 반영되지 않습니다.  
        - 승인 후(IN_PROGRESS)에는 품목 추가가 불가능합니다.
        """
  )
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
    description = """
        **여러 개의 itemId를 받아 일괄 추가하는 요청 형식**

        예시:
        ```json
        {
          "itemIds": [1, 3, 5]
        }
        ```
        """,
    required = true
  )
  public BaseResponse<InventoryItemAddResponse> addInventoryItems(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long inventoryId,
    @Valid @RequestBody InventoryItemAddRequest request
    ) {

    Long memberId = userDetails.getMemberId();

    InventoryItemAddResponse response = inventoryService.addInventoryItems(memberId, inventoryId, request);

    return BaseResponse.success(response);
  }

  @PatchMapping("/{inventoryId}/items/quantities")
  @PreAuthorize("hasPermission('INVENTORY', 'WRITE')")
  @Operation(
    summary = "입고 예정 품목의 목표 입고 수량 일괄 수정 API",
    description = """
        특정 입고 업무(inventoryId)에 등록된 여러 품목들의  
        **목표 입고 수량(targetQuantity)** 을 한 번에 수정합니다.

        승인 요청 전(ASSIGNED 상태)에서만 가능  
        이미 존재하는 Inventory_Item의 quantity 필드만 변경  
        processed_quantity(현재 입고 수량)에는 영향을 주지 않음  
        담당자로 지정된 멤버만 수정 가능  
        """
  )
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
    required = true,
    description = """
      요청 예시:
        ```json
        {
          "quantities": [
            { "inventoryItemId": 10, "targetQuantity": 50 },
            { "inventoryItemId": 11, "targetQuantity": 120 }
          ]
        }
        ```
      """
  )
  public BaseResponse<Void> updateTargetQuantities(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long inventoryId,
    @Valid @RequestBody InventoryTargetQuantityUpdateRequest request
    ) {

    Long memberId = userDetails.getMemberId();

    inventoryService.updateTargetQuantities(memberId, inventoryId, request);

    return BaseResponse.success();
  }
}
