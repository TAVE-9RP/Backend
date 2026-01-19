package com.nexerp.domain.company.controller;

import com.nexerp.domain.company.model.request.CompanyCreateRequest;
import com.nexerp.domain.company.model.request.CompanyLogoUploadRequest;
import com.nexerp.domain.company.model.response.CompanyCreateResponse;
import com.nexerp.domain.company.model.response.CompanyLogoUploadResponse;
import com.nexerp.domain.company.model.response.CompanySearchResponse;
import com.nexerp.domain.company.service.CompanyService;
import com.nexerp.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
              "description": "중복 테스트용 회사"
            }
            """
        )
      )
    )
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

  @PostMapping(value = "/{companyId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
    summary = "회사 로고 업로드",
    description = """
      회사 로고 이미지를 업로드합니다.
      
      - 업로드 파일은 S3에 저장되며, DB에는 **S3 objectKey(imagePath)**만 저장합니다.
      - 프론트 표시용 URL은 **CloudFront 도메인 + objectKey**로 조립한 `logoUrl`로 반환합니다.
      - 허용 포맷: PNG, JPG/JPEG
      - 최대 용량: 2MB
      """)
  @ApiResponse(
    responseCode = "200",
    description = "업로드 성공",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = CompanyLogoUploadResponse.class),
      examples = @ExampleObject(
        name = "성공 예시",
        value = """
          {
            "success": true,
            "data": {
              "objectKey": "assets/company-logos/company_1/7f3a9c9a0b5c4f7a8b9c0d1e2f3a4b5c.png",
              "logoUrl": "https://dxxxx.cloudfront.net/assets/company-logos/company_1/7f3a9c9a0b5c4f7a8b9c0d1e2f3a4b5c.png"
            }
          }
          """
      )
    )
  )
  public BaseResponse<CompanyLogoUploadResponse> uploadCompanyLogo(
    @Parameter(description = "회사 ID", required = true, example = "1")
    @PathVariable Long companyId,

    @Valid @ModelAttribute CompanyLogoUploadRequest request
  ) {
    CompanyLogoUploadResponse result = companyService.updateCompanyLogo(companyId,
      request);

    return BaseResponse.success(result);
  }
}
