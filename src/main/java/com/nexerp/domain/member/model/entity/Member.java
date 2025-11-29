package com.nexerp.domain.member.model.entity;

import com.nexerp.domain.member.model.embeddable.ServicePermissions;
import com.nexerp.domain.member.model.enums.MemberDepartment;
import com.nexerp.domain.member.model.enums.MemberPosition;
import com.nexerp.domain.member.model.enums.MemberRequestStatus;
import com.nexerp.domain.member.model.enums.MemberRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
public class Member {

    // PK: 회원ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    // 로그인용 ID
    @Column(name = "member_login_id", unique = true, nullable = false, length = 50)
    private String loginId;

    // 회원 비밀번호
    @Column(name = "member_password", nullable = false)
    private String password;

    // 회원 이름
    @Column(name = "member_name", nullable = false, length = 100)
    private String name;

    // 이메일
    @Column(name = "member_email", unique = true, length = 100)
    private String email;

    // 신청일
    @Column(name = "member_join_request_date", updatable = false)
    private LocalDateTime joinRequestDate;

    // 가입일
    @Column(name = "member_join_date")
    private LocalDateTime joinDate;

    // ENUM: 부서
    @Enumerated(EnumType.STRING)
    @Column(name = "member_department", length = 50)
    private MemberDepartment department;

    // ENUM: 직급
    @Enumerated(EnumType.STRING)
    @Column(name = "member_position", nullable = false)
    private MemberPosition position;

    // ENUM: 가입 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "member_request_status", nullable = false)
    private MemberRequestStatus requestStatus;

    // FK: 회사 ID
    @Column(name = "company_id", nullable = false)
    private Long companyId;

   // logisticsRole, inventoryRole, managementRole 각각의 권한
    @Embedded
    private ServicePermissions permissions;


    @Builder
    public Member(String loginId, String password, String name, String email, MemberDepartment department, MemberPosition position, Long companyId){
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.department = department;
        this.position = position;
        this.companyId = companyId;

        // 처음 가입 시 가입 상태 기본값 (요청대기)
        this.requestStatus = MemberRequestStatus.PENDING;
    }

    // @PrePersist: DB에 INSERT되기 직전에 호출됨 → 회원 신청일(joinRequestDate) 자동 기록
    @PrePersist
    protected void onCreate() {
        this.joinRequestDate = LocalDateTime.now();
    }

    // 가입 상태 변경 (PENDING/APPROVED/REJECTED 간 전환 허용)

    public void changeRequestStatus(MemberRequestStatus newStatus) {
      if(newStatus == null) {
        return;
      }

      this.requestStatus = newStatus;

      if (newStatus == MemberRequestStatus.APPROVED && this.joinDate == null) {
        this.joinDate = LocalDateTime.now();
      }

      // REJECTED로 바꾼다고 해서 joinDate를 지우지 않는 쪽으로 설계
    }

}
