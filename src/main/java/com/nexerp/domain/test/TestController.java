package com.nexerp.domain.test;

import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test API", description = "Test")
@RestController
@RequestMapping("/test")
public class TestController {

  @GetMapping()
  public BaseResponse<String> testSuccess() {
    String data = "테스트 요청에 성공했습니다.";
    return BaseResponse.success(data);
  }

  @GetMapping("/error/{userId}")
  public BaseResponse<String> testBaseException(@PathVariable Long userId) {
    if (userId.equals(999L)) {
      throw new BaseException(GlobalErrorCode.VALIDATION_ERROR);
    }
    return BaseResponse.success("정상 사용자입니다.");
  }

  @GetMapping("/custom-error")
  public BaseResponse<Integer> testCustomMessageException(@RequestParam int count) {
    if (count > 10) {
      String customMessage = "요청 횟수(" + count + "회)는 10회를 초과할 수 없습니다.";
      throw new BaseException(GlobalErrorCode.VALIDATION_ERROR, customMessage);
    }
    return BaseResponse.success(count);
  }

  @GetMapping("/system-error")
  public String testSystemError() {
    String test = null;
    return test.toString();
  }


  @PostMapping("/validation")
  public BaseResponse<String> testValidation(@Valid @RequestBody TestRequestDto requestDto) {
    String message = "Validation 성공! 이름: " + requestDto.getName() + ", 나이: " + requestDto.getAge();
    return BaseResponse.success(message);
  }

  @GetMapping("/unexpected-error")
  public BaseResponse<Void> testUnexpectedError() {
    String str = null;
    str.length();
    return BaseResponse.success();
  }
}