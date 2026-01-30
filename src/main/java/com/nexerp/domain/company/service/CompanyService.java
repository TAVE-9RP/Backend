package com.nexerp.domain.company.service;

import com.nexerp.domain.company.model.entity.Company;
import com.nexerp.domain.company.model.request.CompanyCreateRequest;
import com.nexerp.domain.company.model.request.CompanyLogoUploadRequest;
import com.nexerp.domain.company.model.response.CompanyCreateResponse;
import com.nexerp.domain.company.model.response.CompanyLogoUploadResponse;
import com.nexerp.domain.company.model.response.CompanySearchResponse;
import com.nexerp.domain.company.repository.CompanyRepository;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CompanyService {

  private final CompanyRepository companyRepository;
  private final CompanyLogoService companyLogoService;

  @Transactional
  public CompanyCreateResponse createCompany(
    CompanyCreateRequest companyCreateRequest) {

    if (companyRepository.existsByName(companyCreateRequest.getName())) {
      throw new BaseException(GlobalErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 회사 이름입니다.");
    }

    Company newCompany = Company.create(
      companyCreateRequest.getName(),
      companyCreateRequest.getIndustryType(),
      companyCreateRequest.getDescription()
    );

    //관리자와 연결 필요
    Company savedCompany = companyRepository.save(newCompany);

    CompanyCreateResponse companyCreateResponse = CompanyCreateResponse.from(
      savedCompany.getId());

    return companyCreateResponse;
  }

  // keyword=""의 경우 전체 리스트 / 없는 키워드는 빈 배열
  public List<CompanySearchResponse> searchCompaniesByName(String keyword) {
    List<Company> companies = companyRepository
      .findByNameContainingIgnoreCaseOrderByNameAsc(keyword);

    return companies.stream()
      .map(c -> CompanySearchResponse.from(c, companyLogoService.buildLogoUrl(c.getImagePath())))
      .collect(Collectors.toList());
  }

  // 멤버 서비스에서 아래 메서드를 통해 소속 회사 조회 기능 추가 예정
  @Transactional(readOnly = true)
  public Company getCompanyEntity(Long companyId) {
    Company company = companyRepository.findById(companyId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "회사를 찾을 수 없습니다."));
    return company;
  }

  // KPI 스케줄러 회사 식별 용도
  @Transactional(readOnly = true)
  public List<Long> getAllCompanyIds() {
    return companyRepository.findAllIds();
  }

  @Transactional
  public CompanyLogoUploadResponse updateCompanyLogo(Long companyId,
    CompanyLogoUploadRequest request) {

    MultipartFile file = request.getFile();
    if (file == null || file.isEmpty()) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "로고 파일이 비어있습니다.");
    }

    Company company = getCompanyEntity(companyId);
    String oldKey = company.getImagePath();

    // 1) S3 업로드
    String newKey = companyLogoService.uploadCompanyLogo(companyId, file);

    // 2) 트랜잭션 결과에 따라 정리
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCompletion(int status) {
        if (status == STATUS_COMMITTED) {
          // 성공 시에만 기존 파일 삭제
          if (oldKey != null && !oldKey.isBlank()) {
            companyLogoService.deleteLogoQuietly(oldKey);
          }
        } else if (status == STATUS_ROLLED_BACK) {
          companyLogoService.deleteLogoQuietly(newKey);
        }
      }
    });

    // 3) DB 저장 (objectKey)
    company.changeImagePath(newKey);

    // 4) 응답
    return CompanyLogoUploadResponse.builder()
      .objectKey(newKey)
      .logoUrl(companyLogoService.buildLogoUrl(newKey))
      .build();
  }
}
