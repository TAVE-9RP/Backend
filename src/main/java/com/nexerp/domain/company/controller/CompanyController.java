package com.nexerp.domain.company.controller;

import com.nexerp.domain.company.model.request.CompanyCreateRequest;
import com.nexerp.domain.company.model.response.CompanyCreateResponse;
import com.nexerp.domain.company.model.response.CompanySearchResponse;
import com.nexerp.domain.company.service.CompanyService;
import com.nexerp.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@Tag(name = "회사 관련 API", description = "생성 / 키워드를 통한 조회(리스트)")
public class CompanyController {

  private final CompanyService companyService;

  @PostMapping
  // 스웨거 전용 어노테이션 Operation
  // jwtAuth이거는 변경 가능
  @Operation(
    summary = "회사 생성 api",
    description = "**멤버 생성 시 회사가 먼저 존재해야 합니다.** "
      + "회사 이름 중복 불가합니다. "
      + " 회사 이름, 업종 필수입니다.",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "추가 입력 정보",
      required = true,
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = CompanyCreateRequest.class),
        examples = @ExampleObject(
          name = "회사 생성 예시",
          value = """
            {
              "name": "테스트 회사",
              "industryType": "물류/운송",
              "description": "중복 테스트용 회사",
              "imagePath": "http://exmple"
            }
            """
        )
      )
    )
  )
  public BaseResponse<CompanyCreateResponse> createCompany(
    @Valid @RequestBody CompanyCreateRequest companyCreateRequest) {
    CompanyCreateResponse companyCreateResponse = companyService.createCompany(
      companyCreateRequest);
    return BaseResponse.success(companyCreateResponse);
  }

  @GetMapping
  @Operation(summary = "키워드를 통한 회사 조회 api",
    description = " **keyword 파라미터 필수** keyword=\"\"의 경우 모든 회사 반환 "
      + "/ 키워드 포함 회사가 없는 경우 빈리스트 반환"
  )
  public BaseResponse<List<CompanySearchResponse>> searchCompanies(
    @RequestParam("keyword") String keyword) {

    List<CompanySearchResponse> result = companyService.searchCompaniesByName(keyword);
    return BaseResponse.success(result);
  }
}
