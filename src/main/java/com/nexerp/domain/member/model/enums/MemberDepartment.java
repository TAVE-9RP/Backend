package com.nexerp.domain.member.model.enums;

// Member 부서 ENUM
public enum MemberDepartment {
    LOGISTICS("물류 부서"),
    INVENTORY("재고 부서"),
    MANAGEMENT("관리 부서(오너)");

    private final String description;

    MemberDepartment(String description) {
        this.description = MemberDepartment.this.description;
    }

    public String getKoreanName() {
        return description;
    }

}
