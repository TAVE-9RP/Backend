package com.nexerp.domain.member.util;

import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;

public class EnumValidatorUtil {

  // 부서 유효성 검사
  public static MemberDepartment validateDepartment(String department) {
    try {
      return MemberDepartment.valueOf(department);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BaseException(GlobalErrorCode.VALIDATION_ERROR, "유효하지 않은 부서 값입니다: " + department);

    }
  }

  // 직급 유효성 검사
  public static MemberPosition validatePosition(String position) {
    try {
      return MemberPosition.valueOf(position);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BaseException(GlobalErrorCode.VALIDATION_ERROR, "유효하지 않은 직급 값입니다: " + position);
    }
  }
}
