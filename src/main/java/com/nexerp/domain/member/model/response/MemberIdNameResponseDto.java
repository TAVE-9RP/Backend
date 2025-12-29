package com.nexerp.domain.member.model.response;

import lombok.Getter;

@Getter
public class MemberIdNameResponseDto {

  private final Long memberId;

  private final String name;

  public MemberIdNameResponseDto(Long memberId, String name) {
    this.memberId = memberId;
    this.name = name;
  }
}
