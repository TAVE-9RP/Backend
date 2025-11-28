package com.nexerp.domain.admin.model.request;

import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class JoinStatusUpdateRequest {

  @NotEmpty
  private List<StatusUpdateUnit> updates;

  @Getter
  public static class StatusUpdateUnit {
    @NotNull(message = "변경시킬 직원의 ID는 필수입니다.")
    private Long memberId;

    @NotNull(message = "변경할 가입 상태는 필수입니다.")
    @Enumerated(EnumType.STRING)
    private String newStatus;
  }


}
