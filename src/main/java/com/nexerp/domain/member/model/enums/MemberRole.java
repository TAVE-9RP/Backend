package com.nexerp.domain.member.model.enums;

import lombok.Getter;

// Member 권한 ENUM
@Getter
public enum MemberRole {
    ALL("오너 권한"),
    WRITE("읽기, 쓰기 권한"),
    READ("읽기 전용 권한");

    private final String description;

    MemberRole(String description) {
        this.description = description;
    }

    public String getKoreanName() {
        return description;
    }
}
