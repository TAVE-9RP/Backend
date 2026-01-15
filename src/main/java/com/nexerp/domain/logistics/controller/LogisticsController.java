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
import com.nexerp.global.config.SwaggerConfig;
import com.nexerp.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/logistics")
@RequiredArgsConstructor
@Tag(name = "출하 업무 관련 API", description = "출하 업무 조회, 생성, 물품 관리 및 출하 수량 반영 등과 관련된 모든 업무")
@SecurityRequirement(name = SwaggerConfig.AT_SCHEME)
public class LogisticsController {

  private final LogisticsService logisticsService;

  // 출하 업무 전체 조회
  @GetMapping
  @PreAuthorize("hasPermission('LOGISTICS', 'READ')")
  @Operation(
    summary = "출하 업무 전체/키워드 조회 API",
    description = """
      회사에 소속된 모든 출하 업무 리스트 중 키워드를 통해 조회합니다.
      - 키워드는 Logistics 제목, 프로젝트 제목, 프로젝트 번호 중에 포함된 Logistics 조회
      - Param의 keyword를 추가하여 조회 합니다. 만약 전체 조회시에는 "" 사용
      - **반환 정보:**
      - logisticsId (출하 업무 id)
      - projectNumber (프로젝트 번호)
      - logisticsTitle (업무명)
      - customer (거래처)
      - requestedAt (요청일)
      - assigneeSummary (프로젝트 기준)
      - logisticsStatus(진행상태)
      """
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "출하 업무 조회 성공",
      content = @Content(
        mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = LogisticsSearchResponse.class)),
        examples = @ExampleObject(
          name = "성공 예시",
          value = """
            [
              {
                "logisticsId": 5,
                "projectNumber": "C01-25-001",
                "logisticsTitle": "삼성 물산 수출 건",
                "customer": "거래처(프로젝트 기준)",
                "assigneeSummary": "김철수 외 1명",
                "requestedAt": "2025-12-21T14:22:00",
                "inventoryStatus": "IN_PROGRESS"
              }
            ]
            """
        )
      )
    )
  })
  public BaseResponse<List<LogisticsSearchResponse>> searchLogisticsCompany(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestParam("keyword") String keyword
  ) {
    Long memberId = userDetails.getMemberId();
    List<LogisticsSearchResponse> result = logisticsService.searchLogisticsCompany(memberId,
      keyword);

    return BaseResponse.success(result);
  }

  // 출하 업무 정보 수정
  @PatchMapping("/{logisticsId}")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  @Operation(
    summary = "출하 공통 정보 저장 및 수정 API",
    description = """
      **물품 추가를 제외한 입고 업무의 공통 정보를 저장 또는 수정합니다.**
      - 해당 업무 할당된 담당자만 가능
      - 상태 검증: ASSIGNED  또는 REJECT 에서만 수정 가능
      - 출하 업무명(title), 업무 설명(description)을 필수
      - URL 경로의 {logisticsId} 값을 통해 수정할 입고 업무를 지정
      - 프로젝트 넘버는 오너가 프로젝트 생성 시 할당되었기에 별도로 지정하지 않습니다.
      수정과 같은 기능을 하기 때문에 수정 시에도 본 API를 활용합니다.
      수정 가능 시점은 입고 승인 요청을 보내기 전까지입니다.
      - **필드 설명**
          - logisticsTitle (업무 이름)
          - logisticsCarrier (이동수단)
          - logisticsCarrierCompany (운송업체)
          - logisticsDescription (업무 설명)
      """,
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "추가 입력 정보",
      required = true,
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = LogisticsUpdateRequest.class),
        examples = @ExampleObject(
          name = "출하 공통 정보 예시",
          value = """
            {
              "logisticsTitle": "버스타고 출근 하기",
              "logisticsCarrier": "대중 교통",
              "logisticsCarrierCompany": "안양대학교",
              "logisticsDescription" : "출근"
            }
            """
        )
      )
    )
  )
  public BaseResponse<Void> updateLogisticsDetails(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @Parameter(
      name = "logisticsId",
      description = "수정할 출하 업무의 ID (URL Path에 포함)",
      required = true,
      example = "12"
    )
    @PathVariable Long logisticsId,
    @Valid @RequestBody LogisticsUpdateRequest logisticsUpdateRequest
  ) {
    Long memberId = userDetails.getMemberId();

    logisticsService.updateLogisticsDetails(memberId, logisticsId, logisticsUpdateRequest);

    return BaseResponse.success();
  }

  // 출하 업무 승인 요청
  @PatchMapping("/{logisticsId}/request-approval")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  @Operation(
    summary = "출하 업무 승인 요청 API",
    description = """
      담당자가 작성을 완료한 출하 업무를 승인 대기(PENDING) 상태로 전환합니다. 
      출하 예정 품목(LogisticsItem)이 1개 이상 존재해야 함
      - **상태 검증**: ASSIGNED  또는 REJECT 에서만 수정 가능
      """
  )
  public BaseResponse<Void> requestLogisticsApproval(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId
  ) {
    Long memberId = userDetails.getMemberId();
    logisticsService.requestLogisticsApproval(memberId, logisticsId);
    return BaseResponse.success();
  }

  // 물품 추가
  @PostMapping("/{logisticsId}/items")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  @Operation(
    summary = "출하 예정 품목 추가 API",
    description = """
      출하 업무에 포함될 물품들을 ID 리스트 형식으로 추가합니다.
      - **중복 방지**: 이미 등록된 itemId는 자동으로 제외됩니다.
      - **상태 검증**: ASSIGNED 또는 REJECT 에서만 수정 가능
      
      **출하 예정 품목(Llogistics_item) 생성 전용 API**
      품목의 목표 입고 수량은 이 API에서 입력하지 않으며,  
      별도의 '목표 수량 설정 API'에서 진행합니다.  
      이미 목록에 존재하는 품목은 자동으로 제외됩니다.  
      품목은 '승인 요청' 이전 상태(ASSIGNED)에서만 추가가 가능합니다. 
      
      주의  
      - 실제 재고(Item.quantity) 수량에는 반영되지 않습니다.  
      - 승인 후(IN_PROGRESS)에는 품목 추가가 불가능합니다.
      """,
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "물품 입력 정보",
      required = true,
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = LogisticsItemsCreateRequest.class),
        examples = @ExampleObject(
          name = "출하 예정 물품 추가 요청 예시",
          value = """
            {
              "itemIds": [1, 2, 3]
            }
            """
        )
      )
    )
  )
  public BaseResponse<Void> createLogisticsItem(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId,
    @Valid @RequestBody LogisticsItemsCreateRequest request
  ) {
    Long memberId = userDetails.getMemberId();
    logisticsService.addLogisticsItems(memberId, logisticsId, request.getItemIds());
    return BaseResponse.success();
  }

  // 출하 물품 조회
  @GetMapping("/{logisticsId}/items")
  @PreAuthorize("hasPermission('LOGISTICS', 'READ')")
  @Operation(
    summary = "출하 물품 목록 조회 API",
    description = """
      특정 출하 업무(logisticsId)에 등록된 모든 **출하 품목(Logistics_Item)** 목록을 조회합니다.
      LogisticsItem
      - **반환 정보:**
      - logisticsItemId (출하 재고 id)
      - itemId (재고 id)
      - itemCode (재고 번호)
      - itemName (재고 이름)
      - itemPrice (재고 가격)
      - targetedQuantity (목표 수량)
      - processedQuantity (현재 수량)
      - itemTotalPrice (총 가격 [목표수량 * 가격])
      - logisticsProcessingStatus (진행상태)
      """
  )
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
  @Operation(
    summary = "실제 출하 처리(수량 반영) API",
    description = """
      실제로 출하된 물품의 수량을 반영합니다.
      IN_PROGRESS 상태에서만 가능
      Item 재고(quantity) 감소
      - **재고 반영**: 입력한 수량만큼 실제 재고(Item) 수량이 변경됩니다.
      - **상태 변화**: 
        - **진행 수량 >= 목표 수량** 인 경우 해당 물품은 **COMPLETED** 처리됩니다 + **출하일 설정**
      """,
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "물품 입력 정보",
      required = true,
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = LogisticsItemsUpdateRequest.class),
        examples = @ExampleObject(
          name = "출하 예시",
          value = """
            {
              "items": [
                { "logisticsItemId": 1, "processedQuantity": 1 },
                { "logisticsItemId": 2, "processedQuantity": 1 }
              ]
            }
            """
        )
      )
    )
  )
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
  @Operation(
    summary = "출하 업무 상세 정보 조회 API",
    description = """
      출하 업무의 공통 정보와 담당자 리스트 등 상세 내용을 조회합니다.
      - **반환 정보:**
      - projectNumber (프로젝트 번호)
      - logisticsAssignees  (출하 업무 담당자 전체)
      - logisticsTitle (업무 이름)
      - logisticsDescription (출하 설명)
      - logisticsCarrier (운송수단)
      - logisticsCarrierCompany (운송업체)
      - logisticsRequestedAt (요청일)
      - logisticsCompletedAt (완료일)
      - logisticsStatus (진행상태)
      """
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "출하 업무 상세 조회 성공",
      content = @Content(
        mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = LogisticsDetailsResponse.class)),
        examples = @ExampleObject(
          name = "성공 예시",
          value = """
            [
              {
                "projectNumber": "C01-25-001",
                "logisticsAssignees": ["윤민섭", "곽채연"],
                "logisticsTitle": "삼성 물산 수출 건",
                "logisticsDescription": "과일류 출하 처리",
                "logisticsCarrier": "이원진",
                "logisticsCarrierCompany": "TAVE",
                "logisticsRequestedAt": "2025-12-21T14:22:00",
                "logisticsStatus": "IN_PROGRESS"
              }
            ]
            """
        )
      )
    )
  })
  public BaseResponse<LogisticsDetailsResponse> getLogisticsDetails(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId
  ) {
    Long memberId = userDetails.getMemberId();
    LogisticsDetailsResponse response = logisticsService.getLogisticsDetails(memberId, logisticsId);
    return BaseResponse.success(response);
  }

  //업무 완료 처리
  @PatchMapping("/{logisticsId}/complete")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  @Operation(
    summary = "출하 업무 최종 완료 처리 API",
    description = """
      모든 출하 품목이 완료 상태(COMPLETED)일 때
      담당자가 '출하 완료' 버튼을 눌러 업무 상태를 COMPLETED로 변경합니다.
      
      - 진행 중(IN_PROGRESS) 상태에서만 수행 가능
      - 하나라도 미완료 품목이 있으면 실패
      - 담당자로 지정된 직원만 가능
      """
  )
  public BaseResponse<Void> completeLogistics(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId
  ) {
    Long memberId = userDetails.getMemberId();
    logisticsService.completeLogistics(memberId, logisticsId);
    return BaseResponse.success();
  }

  // 목표 출하 수량
  @PatchMapping("/{logisticsId}/items/targetQuantity")
  @PreAuthorize("hasPermission('LOGISTICS', 'WRITE')")
  @Operation(
    summary = "출하 예정 품목의 목표 출하 수량 일괄 수정 API",
    description = """
      특정 입고 업무(logisticsId)에 등록된 여러 품목들의  
      **목표 입고 수량(targetQuantity)** 을 한 번에 수정합니다.
      
      승인 요청 전(ASSIGNED 상태)에서만 가능  
      이미 존재하는 logisticsItem의 targetQuantity 필드만 변경  
      processed_quantity(현재 입고 수량)에는 영향을 주지 않음  
      담당자로 지정된 멤버만 수정 가능  
      
      - **상태 검증**: ASSIGNED 또는 REJECT 에서만 수정 가능
      - 요청 시 누락된 물품은 기존 수량을 유지합니다.
      """,
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "물품 입력 정보",
      required = true,
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = LogisticsItemTargetQuantityRequest.class),
        examples = @ExampleObject(
          name = "목표 수량 일괄 수정 요청 형식",
          value = """
            {
              "items": [
                { "logisticsItemId": 1, "targetQuantity": 1 },
                { "logisticsItemId": 3, "targetQuantity": 5 }
              ]
            }
            
            """
        )
      )
    )
  )
  public BaseResponse<Void> updateTargetQuantities(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @PathVariable Long logisticsId,
    @Valid @RequestBody LogisticsItemTargetQuantityRequest request
  ) {
    Long memberId = userDetails.getMemberId();
    logisticsService.updateTargetQuantities(memberId, logisticsId, request.getItems());
    return BaseResponse.success();
  }

  @GetMapping("/assigned")
  @Operation(
    summary = "프로젝트 기준 속한 본인이 출하 업무 키워드 리스트 조회 api",
    description = """
      본인이 소속된 프로젝트의 모든 출하 업무 리스트를 키워드를 통해 조회합니다.
      - 키워드는 Logistics 제목, 프로젝트 제목, 프로젝트 번호 중에 포함된 Logistics 조회
      - Param의 keyword를 추가하여 조회 합니다. 만약 전체 조회시에는 "" 사용
      - **반환 정보:**
      - logisticsId (출하 업무 id)
      - projectNumber (프로젝트 번호)
      - logisticsTitle (업무명)
      - customer (거래처)
      - requestedAt (요청일)
      - assigneeSummary (프로젝트 기준)
      - logisticsStatus(진행상태)
      """
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "출하 업무 조회 성공",
      content = @Content(
        mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = LogisticsSearchResponse.class)),
        examples = @ExampleObject(
          name = "성공 예시",
          value = """
            [
              {
                "logisticsId": 5,
                "projectNumber": "C01-25-001",
                "logisticsTitle": "삼성 물산 수출 건",
                "customer": "거래처(프로젝트 기준)",
                "assigneeSummary": "김철수 외 1명",
                "requestedAt": "2025-12-21T14:22:00",
                "logisticsStatus": "IN_PROGRESS"
              }
            ]
            """
        )
      )
    )
  })
  public BaseResponse<List<LogisticsSearchResponse>> searchLogisticsAssignees(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestParam("keyword") String keyword
  ) {
    Long memberId = userDetails.getMemberId();
    List<LogisticsSearchResponse> result = logisticsService.searchLogisticsAssignees(memberId,
      keyword);

    return BaseResponse.success(result);
  }
}
