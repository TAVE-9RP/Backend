package com.nexerp.domain.admin.model.request;

import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class JoinStatusUpdateRequest {
  @NotNull(message = "변경할 가입 상태는 필수입니다.")
  private MemberRequestStatus newStatus;
}
