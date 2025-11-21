package com.nexerp.global.security.details;

import com.nexerp.domain.member.model.embeddable.ServicePermissions;
import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// UserDetails 구현체
// DB의 Member 엔티티를 스프링 시큐리티 객체로 변환
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final Member member;

    // 이미 DB에서 가져온 Member 엔티티 반환해서 Service, Controller 사용하기 위한 용도
    public Member getMember() {
        return member;
    }

    /**
     * DB의 Member 엔티티 정보 가져옴
     */
    public Long getMemberId(){
        return member.getId();
    }

    @Override
    public String getUsername() {
        return member.getLoginId();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    /**
     * 계정 상태 확인: MVP에서는 true로 설정
     */
    // 계정이 만료되지 않았는지 확인
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정이 잠겨있지 않은지 확인
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료되지 않았는지 확인
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화 상태인지 확인
    @Override
    public boolean isEnabled() {
//        return member.getRequestStatus() == MemberRequestStatus.APPROVED;
        return true;
    }

    // JWT 페이로드에 들어갈 권한 목록 반환 (직급 + 세부 업무 권한)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 직급 권한 (ex. ROLE_INTERN)
        String primaryRoleName = member.getPosition().name();
        String primaryRole = "ROLE_" + primaryRoleName;
        authorities.add(new SimpleGrantedAuthority(primaryRole));

        // 세부 업무 권한 (ex. LOGISTICS_WRITE, INVENTORY_READ)
        ServicePermissions permissions = member.getPermissions();

        if (permissions != null) {
            if(permissions.getLogisticsRole() != null){
                String logisticsRole = "LOGISTICS_" + permissions.getLogisticsRole().name();
                authorities.add(new SimpleGrantedAuthority(logisticsRole));
            }

            if(permissions.getInventoryRole() != null){
                String inventoryRole = "INVENTORY_" + permissions.getInventoryRole().name();
                authorities.add(new SimpleGrantedAuthority(inventoryRole));
            }

            if(permissions.getManagementRole() != null){
                String managementRole = "MANAGEMENT_" + permissions.getManagementRole().name();
                authorities.add(new SimpleGrantedAuthority(managementRole));
            }
        }
        return authorities;
    }

}
