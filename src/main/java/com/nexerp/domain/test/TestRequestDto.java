package com.nexerp.domain.test;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class TestRequestDto {

  @NotBlank(message = "이름은 필수 입력 항목입니다.")
  @Size(min = 2, max = 10, message = "이름은 2자 이상 10자 이하여야 합니다.")
  private String name;

  @NotNull(message = "나이는 필수 입력 항목입니다.")
  @Min(value = 1, message = "나이는 1살 이상이어야 합니다.")
  private Integer age;
}
