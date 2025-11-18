package com.nexerp.domain.member.model.enums;

import lombok.Getter;

// Member 가입 상태 ENUM
@Getter
public enum MemberRequestStatus {
    PENDING("요청대기"),
    APPROVED("승인"),
    REJECTED("거절");

    private final String description;

    MemberRequestStatus(String description) {
        this.description = description;
    }

    public String getKoreanName() {
        return description;
    }

}
