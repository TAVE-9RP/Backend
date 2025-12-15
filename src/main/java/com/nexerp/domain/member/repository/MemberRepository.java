package com.nexerp.domain.member.repository;

import com.nexerp.domain.member.model.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  // 회원가입 시 중복 검사  (로그인용 Id, 이메일)
  Optional<Member> findByLoginId(String loginId);

  Optional<Member> findByEmail(String email);

}
