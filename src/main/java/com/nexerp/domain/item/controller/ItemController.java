package com.nexerp.domain.item.controller;

import com.nexerp.domain.item.model.request.ItemCreateRequest;
import com.nexerp.domain.item.model.request.ItemSafetyStockUpdateRequest;
import com.nexerp.domain.item.model.request.ItemTargetStockUpdateRequest;
import com.nexerp.domain.item.model.response.ItemCreateResponse;
import com.nexerp.domain.item.model.response.ItemDetailResponse;
import com.nexerp.domain.item.model.response.ItemHistoryResponse;
import com.nexerp.domain.item.model.response.ItemSearchResponse;
import com.nexerp.domain.item.service.ItemService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.config.SwaggerConfig;
import com.nexerp.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Tag(name = "품목 관련 API", description = "품목 추가, 조회 등")
@SecurityRequirement(name = SwaggerConfig.AT_SCHEME)
public class ItemController {

  private final ItemService itemService;

  @PreAuthorize("hasPermission('INVENTORY', 'WRITE')")
  @PostMapping
  @Operation(
    summary = "신규 품목(Item) 생성 API",
    description = """
      로그인한 직원이 속한 **자신의 회사**에 새로운 품목(Item)를 생성합니다.
      신규 품목(Item)을 생성합니다.
      
      ✔ quantity는 항상 0으로 생성됩니다.
      ✔ 승인 전이므로 실제 품목 반영이 아닙니다.
      ✔ 생성 후 반환된 itemId로 입고 목록 추가 API를 바로 호출하면 됩니다.
      """,
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      content = @Content(
        schema = @Schema(implementation = ItemCreateRequest.class),
        examples = @ExampleObject(value = """
              {
                 "code": "MAT-001",
                 "name": "새로운 부품",
                 "location": "A-02",
                 "price": 1500
              }
          """)
      )
    )
  )
  public BaseResponse<ItemCreateResponse> createItem(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody ItemCreateRequest request
  ) {
    Long memberId = userDetails.getMemberId();
    ItemCreateResponse response = itemService.createItem(memberId, request);
    return BaseResponse.success(response);
  }


  @GetMapping
  @Operation(
    summary = "기존 물품 검색 API",
    description = """
      로그인한 직원이 속한 **자신의 회사 물품만** 검색합니다.
      
      기존 물품(Item)를 키워드로 검색합니다.
      ✔ 물품 번호(code), 물품명(name), 위치(location) 등을 부분 검색합니다.
      ✔ keyword가 비어 있으면 전체 물품을 반환합니다.
      ✔ INVENTORY READ 권한이 필요합니다.
      """
  )
  public BaseResponse<List<ItemSearchResponse>> searchItems(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestParam(required = false) String keyword
  ) {
    Long memberId = userDetails.getMemberId();

    List<ItemSearchResponse> results = itemService.searchItems(memberId, keyword);

    return BaseResponse.success(results);
  }

  @Operation(
    summary = "품목 상세 조회",
    description = """
      특정 물품 상세 정보(물품, 가격, 위치, 목표/안전 재고 등)를 조회합니다.
      - **반환 정보:**
      - itemId (품목 식별자 ID)
      - code (품목 코드)
      - name (품목 이름)
      - quantity (현재 재고 수량)
      - price (품목 출하 단가)
      - location (창고 내 위치)
      - createdAt (품목 등록 일시)
      - targetStock (목표 재고 수량)
      - safetyStock (안전 재고 수량)
      """
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "요청에 성공했습니다.")
  })
  @GetMapping("/{itemId}")
  public BaseResponse<ItemDetailResponse> getItemDetail(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long itemId
  ) {
    Long memberId = userDetails.getMemberId();

    ItemDetailResponse result = itemService.getItemDetail(memberId, itemId);
    return BaseResponse.success(result);
  }

  @Operation(
    summary = "품목 이력 조회",
    description = "특정 품목의 재고 변동 및 업무 처리 이력을 리스트로 조회합니다."
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "요청에 성공했습니다.",
      content = @Content(
        mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = ItemHistoryResponse.class)),
        examples = @ExampleObject(
          name = "성공 예시",
          value = """
            [
              {
                "itemHistoryId": 105,
                "itemId": 1,
                "taskType": "LOGISTICS",
                "memberId": 5,
                "memberName": "홍길동",
                "processedAt": "2024-03-21T14:30:00",
                "changeQuantity": -10
              },
              {
                "itemHistoryId": 102,
                "itemId": 1,
                "taskType": "INVENTORY",
                "memberId": 3,
                "memberName": "이영희",
                "processedAt": "2024-03-20T10:00:00",
                "changeQuantity": 50
              }
            ]
            """
        )
      )
    )
  })
  @GetMapping("/{itemId}/history")
  public BaseResponse<List<ItemHistoryResponse>> getItemHistories(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long itemId
  ) {
    Long memberId = userDetails.getMemberId();
    List<ItemHistoryResponse> results = itemService.getItemHistories(memberId, itemId);
    return BaseResponse.success(results);
  }

  @Operation(
    summary = "목표 재고 수량 수정",
    description = "특정 물품의 목표 재고 수량(Target Stock)을 수정합니다. targetStock은 필수이며 0 이상이어야 합니다."
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = BaseResponse.class),
        examples = @ExampleObject(
          name = "성공 예시",
          value = """
            {
                  "timestamp": "2025-12-26T18:14:23.244638700Z",
                  "isSuccess": true,
                  "status": 200,
                  "code": "SUCCESS",
                  "message": "요청에 성공했습니다."
              }
            """
        )
      )
    )
  })
  @PreAuthorize("hasPermission('INVENTORY', 'WRITE')")
  @PatchMapping("/{itemId}/target-stock")
  public BaseResponse<Void> updateItemTargetStock(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long itemId,
    @Valid @RequestBody ItemTargetStockUpdateRequest request
  ) {
    Long memberId = userDetails.getMemberId();
    itemService.updateItemTargetStock(memberId, itemId, request.getTargetStock());
    return BaseResponse.success();
  }

  @Operation(
    summary = "안전 재고 수량 수정",
    description = "특정 물품의 목표 재고 수량(Safety Stock)을 수정합니다. safetyStock은 필수이며 0 이상이어야 합니다."
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = BaseResponse.class),
        examples = @ExampleObject(
          name = "성공 예시",
          value = """
            {
                  "timestamp": "2025-12-26T18:14:23.244638700Z",
                  "isSuccess": true,
                  "status": 200,
                  "code": "SUCCESS",
                  "message": "요청에 성공했습니다."
              }
            """
        )
      )
    )
  })
  @PreAuthorize("hasPermission('INVENTORY', 'WRITE')")
  @PatchMapping("/{itemId}/safety-stock")
  public BaseResponse<Void> updateItemTargetStock(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long itemId,
    @Valid @RequestBody ItemSafetyStockUpdateRequest request
  ) {
    Long memberId = userDetails.getMemberId();
    itemService.updateItemSafetyStock(memberId, itemId, request.getSafetyStock());
    return BaseResponse.success();
  }
}
