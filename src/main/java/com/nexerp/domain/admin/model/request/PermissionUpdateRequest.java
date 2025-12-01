package com.nexerp.domain.admin.model.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class PermissionUpdateRequest {
  @NotEmpty
  private List<PermissionUpdateUnit> updates;

  @Getter
  public static class PermissionUpdateUnit {

    @NotNull
    private Long memberId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private String newRole; // WRITE 또는 READ
  }
}
