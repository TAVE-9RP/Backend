package com.nexerp.domain.item.controller;

import com.nexerp.domain.item.model.request.ItemCreateRequest;
import com.nexerp.domain.item.model.response.ItemCreateResponse;
import com.nexerp.domain.item.service.ItemService;
import com.nexerp.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

  private final ItemService itemService;

  @PreAuthorize("hasPermission('INVENTORY', 'WRITE')")
  @PostMapping
  @Operation(
    summary = "신규 재고(Item) 생성 API",
    description = """
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
    @Valid @RequestBody ItemCreateRequest request
  ) {
    ItemCreateResponse response = itemService.createItem(request);
    return BaseResponse.success(response);
  }
}
