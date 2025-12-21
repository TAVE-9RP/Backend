package com.nexerp.domain.item.controller;

import com.nexerp.domain.item.model.request.ItemCreateRequest;
import com.nexerp.domain.item.model.response.ItemCreateResponse;
import com.nexerp.domain.item.model.response.ItemSearchResponse;
import com.nexerp.domain.item.service.ItemService;
import com.nexerp.global.common.response.BaseResponse;
import com.nexerp.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Tag(name = "물품 관련 API", description = "물품 추가, 조회")
public class ItemController {

  private final ItemService itemService;

  @PreAuthorize("hasPermission('INVENTORY', 'WRITE')")
  @PostMapping
  @Operation(
    summary = "신규 재고(Item) 생성 API",
    description = """
            로그인한 직원이 속한 **자신의 회사**에 새로운 재고(Item)를 생성합니다.
            신규 품목(Item)을 생성합니다.
            
            ✔ quantity는 항상 0으로 생성됩니다.
            ✔ 승인 전이므로 실제 재고 반영이 아닙니다.
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
  public BaseResponse<ItemCreateResponse> createItem (
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @Valid @RequestBody ItemCreateRequest request
  ) {
    Long memberId = userDetails.getMemberId();
    ItemCreateResponse response = itemService.createItem(memberId, request);
    return BaseResponse.success(response);
  }


  @GetMapping
  @Operation(
    summary = "기존 재고 검색 API",
    description = """
      로그인한 직원이 속한 **자신의 회사 재고만** 검색합니다.
      
      기존 재고(Item)를 키워드로 검색합니다.
      ✔ 재고 번호(code), 품목명(name), 위치(location) 등을 부분 검색합니다.
      ✔ keyword가 비어 있으면 전체 재고를 반환합니다.
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
}
