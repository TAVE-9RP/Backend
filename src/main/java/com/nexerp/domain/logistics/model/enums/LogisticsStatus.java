package com.nexerp.domain.logistics.model.enums;

import lombok.Getter;

@Getter
public enum LogisticsStatus {
  ASSIGNED,
  APPROVAL_PENDING,
  IN_PROGRESS,
  COMPLETED;
}
