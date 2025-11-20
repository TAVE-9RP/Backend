package com.nexerp.domain.member.model.enums;

import lombok.Getter;

// Member 직급 ENUM
@Getter
public enum MemberPosition {
    INTERN("인턴"),
    ASSISTANT_MANAGER("주임"),
    MANAGER("대리"),
    SENIOR_MANAGER("과장"),
    DEPARTMENT_HEAD("부장"),
    OWNER("오너");

    private final String description;

    MemberPosition(String description) {
        this.description = description;
    }

    public String getKoreanName() {
        return description;
    }
}
