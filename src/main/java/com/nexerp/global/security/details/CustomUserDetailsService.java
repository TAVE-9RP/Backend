package com.nexerp.global.security.details;

import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.repository.MemberRepository;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

// 사용자의 ID로 DB를 조회하고, 결과를 CustomUserDetails 객체로 반환
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Optional<Member> memberOptional =  memberRepository.findByLoginId(loginId);

        if(memberOptional.isPresent()){
            Member member = memberOptional.get();
            return new CustomUserDetails(member);
        }
        else{
            throw new UsernameNotFoundException("해당하는 사용자를 찾을 수 없습니다: " + loginId);
        }
    }

    public Authentication createAuthenticationById(String memberId) {
        // DB에서 Member 조회
        Member member = memberRepository.findById(Long.valueOf(memberId))
          .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "회원이 존재하지 않습니다."));

        // DB에서 가져온 Member 정보로 CustomUserDetails를 생성하여 Authentication 객체를 만듦
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // CustomUserDetails의 권한 정보를 Authentication 객체에 담기
        return new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities()
        );
    }
}
