package com.nexerp.domain.member.model.enums;

// Member 가입 상태 ENUM
public enum MemberRequestStatus {
    PENDING("요청대기"),
    APPROVED("승인"),
    REJECTED("거절");

    private final String description;

    MemberRequestStatus(String description) {
        this.description = MemberRequestStatus.this.description;
    }

    public String getKoreanName() {
        return description;
    }

}
