package com.flux.movieproject.repository.member;

import com.flux.movieproject.model.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
	Optional<Member> findByEmail(String email);

	Optional<Member> findByEmailAndPhone(String email, String phone);

	Optional<Member> findByUsername(String username);

	Optional<Member> findByThirdPartyLoginId(String thirdPartyLoginId);

	Optional<Member> findById(Integer id);

	Member findByMemberId(Integer memberId);
}
