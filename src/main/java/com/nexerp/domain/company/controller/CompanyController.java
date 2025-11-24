package com.nexerp.domain.company.controller;

import com.nexerp.domain.company.model.request.CompanyCreateRequest;
import com.nexerp.domain.company.model.response.CompanyCreateResponse;
import com.nexerp.domain.company.model.response.CompanySearchResponse;
import com.nexerp.domain.company.service.CompanyService;
import com.nexerp.global.common.response.BaseResponse;
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
public class CompanyController {

  //hasRole 추가 예정
  private final CompanyService companyService;

  @PostMapping("/create")
  public BaseResponse<CompanyCreateResponse> createCompany(
    @Valid @RequestBody CompanyCreateRequest companyCreateRequest) {
    CompanyCreateResponse companyCreateResponse = companyService.createCompany(
      companyCreateRequest);
    return BaseResponse.success(companyCreateResponse);
  }

  @GetMapping("/search")
  public BaseResponse<List<CompanySearchResponse>> searchCompanies(
    @RequestParam("keyword") String keyword) {

    List<CompanySearchResponse> result = companyService.searchCompaniesByName(keyword);
    return BaseResponse.success(result);
  }
}
