package com.nexerp.domain.admin.model.response;


import com.nexerp.domain.member.model.entity.Member;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminInfoResponse {

  private final Long companyId;
  private final Long adminId;
  private final String adminName;
  private final String adminEmail;

  public static AdminInfoResponse from(Member admin) {
    return AdminInfoResponse.builder()
      .companyId(admin.getCompanyId())
      .adminId(admin.getId())
      .adminName(admin.getName())
      .adminEmail(admin.getEmail())
      .build();
  }

  public static List<AdminInfoResponse> listFrom(List<Member> admins) {
    return admins.stream()
      .map(AdminInfoResponse::from)
      .collect(Collectors.toList());
  }
}
