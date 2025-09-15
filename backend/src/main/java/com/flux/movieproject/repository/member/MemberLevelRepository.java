package com.flux.movieproject.repository.member;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flux.movieproject.model.entity.member.MemberLevel;

@Repository
public interface MemberLevelRepository extends JpaRepository<MemberLevel, Integer> {

	/**
	 * 查詢所有會員等級，並根據等級門檻由高至低排序。
	 * 
	 * @return
	 */
	List<MemberLevel> findAllByOrderByThresholdLowerBoundDesc();
}
