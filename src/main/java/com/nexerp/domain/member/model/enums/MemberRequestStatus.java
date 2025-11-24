package com.nexerp.domain.member.model.enums;

import lombok.Getter;

// Member 가입 상태 ENUM
@Getter
public enum MemberRequestStatus {
    PENDING,
    APPROVED,
    REJECTED;

}
