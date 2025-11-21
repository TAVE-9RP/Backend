package com.nexerp.domain.member.controller;

import com.nexerp.domain.member.model.request.MemberSignupRequestDto;
import com.nexerp.domain.member.service.MemberService;
import com.nexerp.global.common.exception.GlobalErrorCode;
import com.nexerp.global.common.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public BaseResponse<Long> signUp(@RequestBody @Valid MemberSignupRequestDto memberSignupRequestDto){

        Long memberId = memberService.signUp(memberSignupRequestDto);

        return BaseResponse.success(memberId);

    }
}
