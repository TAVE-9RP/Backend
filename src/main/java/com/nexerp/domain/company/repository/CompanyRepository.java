package com.nexerp.domain.company.repository;

import com.nexerp.domain.company.model.entity.Company;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

  // 이름 필드의 특정 문자열을 포함하는 데이터 조회(대소문자 구분 X)
  List<Company> findByNameContainingIgnoreCaseOrderByNameAsc(String keyword);

}
