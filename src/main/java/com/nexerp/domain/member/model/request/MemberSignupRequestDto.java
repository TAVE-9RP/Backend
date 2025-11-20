package com.nexerp.domain.member.model.request;

import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import com.nexerp.domain.member.model.enums.MemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
// 회원가입 요청 DTO
public class MemberSignupRequestDto {

    // 로그인용 ID
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Size(min = 4, max = 50, message = "아이디는 4자 이상 50자 이내여야 합니다.")
    private String loginId;

    // 회원 비밀번호
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    // 회원 이름
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다.")
    private String name;

    // 이메일
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email;

    // ENUM: 부서
    @NotNull(message = "부서는 필수 입력 값입니다.")
    private MemberDepartment department;

    // ENUM: 직급
    @NotNull(message = "직급은 필수 입력 값입니다.")
    private MemberPosition position;

    // FK: 회사 ID
    @NotNull(message = "회사 ID는 필수 입력 값입니다.")
    private Long companyId;
}
