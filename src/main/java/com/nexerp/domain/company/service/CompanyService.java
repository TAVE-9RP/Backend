package com.nexerp.domain.company.service;

import com.nexerp.domain.company.model.entity.Company;
import com.nexerp.domain.company.model.request.CompanyCreateRequest;
import com.nexerp.domain.company.model.response.CompanyCreateResponse;
import com.nexerp.domain.company.model.response.CompanySearchResponse;
import com.nexerp.domain.company.repository.CompanyRepository;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

  private final CompanyRepository companyRepository;

  @Transactional
  public CompanyCreateResponse createCompany(
    CompanyCreateRequest companyCreateRequest) {

    if (companyRepository.existsByName(companyCreateRequest.getName())) {
      throw new BaseException(GlobalErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 회사 이름입니다.");
    }

    Company newCompany = Company.create(
      companyCreateRequest.getName(),
      companyCreateRequest.getIndustryType(),
      companyCreateRequest.getDescription(),
      companyCreateRequest.getImagePath()
    );

    //관리자와 연결 필요
    Company savedCompany = companyRepository.save(newCompany);

    CompanyCreateResponse companyCreateResponse = CompanyCreateResponse.from(
      savedCompany.getId());

    return companyCreateResponse;
  }

  // keyword=""의 경우 전체 리스트 / 없는 키워드는 빈 배열
  @Transactional(readOnly = true)
  public List<CompanySearchResponse> searchCompaniesByName(String keyword) {
    List<Company> companies = companyRepository
      .findByNameContainingIgnoreCaseOrderByNameAsc(keyword);

    return companies.stream()
      .map(CompanySearchResponse::from)
      .collect(Collectors.toList());
  }

  // 멤버 서비스에서 아래 메서드를 통해 소속 회사 조회 기능 추가 예정
  @Transactional(readOnly = true)
  public Company getCompanyEntity(Long companyId) {
    Company company = companyRepository.findById(companyId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "회사를 찾을 수 없습니다."));
    return company;
  }

}
