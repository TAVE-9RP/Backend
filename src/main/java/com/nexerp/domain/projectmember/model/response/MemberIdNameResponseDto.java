package com.nexerp.domain.projectmember.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberIdNameResponseDto {

  private final Long memberId;

  private final String name;

}
