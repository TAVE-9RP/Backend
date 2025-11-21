package com.nexerp.domain.member.service;

import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import com.nexerp.domain.member.model.request.MemberSignupRequestDto;
import com.nexerp.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public Long signUp(MemberSignupRequestDto memberSignupRequestDto){
        // 1. 중복 검사: 아이디
        if(memberRepository.findByLoginId(memberSignupRequestDto.getLoginId()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 아이디입니다. ");
        }

        // 2. 중복 검사: 이메일
        if(memberRepository.findByEmail(memberSignupRequestDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일입니다. ");
        }

        String encodedPassword = passwordEncoder.encode(memberSignupRequestDto.getPassword());

        Member member = Member.builder()
                .loginId(memberSignupRequestDto.getLoginId())
                .password(encodedPassword)
                .name(memberSignupRequestDto.getName())
                .email(memberSignupRequestDto.getEmail())
                .department(memberSignupRequestDto.getDepartment())
                .position(memberSignupRequestDto.getPosition())
                .companyId(memberSignupRequestDto.getCompanyId())
                .build();

        Member savedMember = memberRepository.save(member);

        return savedMember.getId();

    }
}
