package com.nexerp.domain.admin.model.response;

import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinStatusResponse {

    private final Long memberId;
    private final String name;
    private final MemberDepartment department;
    private final MemberPosition position;
    private final String email;
    private final MemberRequestStatus requestStatus;

    public static JoinStatusResponse from(Member member) {
      return JoinStatusResponse.builder()
        .memberId(member.getId())
        .name(member.getName())
        .department(member.getDepartment())
        .position(member.getPosition())
        .email(member.getEmail())
        .requestStatus(member.getRequestStatus())
        .build();
    }
}
